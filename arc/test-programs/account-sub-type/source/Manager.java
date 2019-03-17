public class Manager extends Thread {
  Bank bank;
  Account account;
  int accountNumber;

  public Manager(Bank b, Account a, int n) {
    bank = b;
    account = a;
    accountNumber = n;
  }

  // Perform a sequence of operations on the account
  public void run(){
    int nextNumber;

    //System.out.println("Manager for "+ account.getName() + " starting work");
    account.deposit(300);

    nextNumber = bank.nextAccountNumber(accountNumber);

    if (nextNumber - accountNumber != 1)
      System.out.println("nextNumber - accountNumber is " + Integer.toString(nextNumber - accountNumber));

    // FIX:
    // 1. In PersonalAccount.transfer, ac.amount+=mn has to be
    //    synchronized on ac
    // 2. In Manager.run, both of the account.transfer lines run have to be
    //    synchronized on bank
    account.transfer(bank.getAccount(nextNumber),10);

    account.deposit(10);

    account.withdraw(20);

    account.deposit(10);

    // FIX:
    // 1. In PersonalAccount.transfer, ac.amount+=mn has to be
    //    synchronized on ac
    // 2. In Manager.run, both of the account.transfer lines run have to be
    //    synchronized on bank
    account.transfer(bank.getAccount(nextNumber),10);

    account.withdraw(100);
    //System.out.println("Manager for "+ account.getName() + " done with work");
  }

}