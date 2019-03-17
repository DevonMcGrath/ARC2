public class PersonalAccount extends Account {
  public PersonalAccount(int number, int initialBalance) {
    super(number, initialBalance);
  }

  // FIX:
  // 1. In PersonalAccount.transfer, ac.amount+=mn has to be
  //    synchronized on ac
  // 2. In Manager.run, both of the account.transfer lines run have to be
  //    synchronized on bank
  public synchronized void transfer(Account ac, int mn){
    amount-=mn;
    ac.amount+=mn;
  }
}