/**
 * This is a completely rewritten version of the IBM "account" example.
 * It displays the same bug, but it makes the bug a bit more subtle by
 * realizing the error in one of two possible sub-types of "Account".
 *
 * @author Matt Dwyer
 */

// DK
// FIX:
// 1. In PersonalAccount, ac.amount+=mn has to be synchronized on ac
// 2. In Manager, both of the account.transfer lines run have to be
//    synchronized on bank

import java.io.*; // DK
import java.util.*;

public class Main {

  /***
   * @params args[0] is the (optional) number of correct accounts to create
   * @params args[1] is the (optional) number of buggy accounts to create
   */
  public static void main(String[] args) {
    int numBusinessAccounts = 2;  // DK: was 10
    int numPersonalAccounts = 1;
    boolean everythingBalances = true;

    if (args != null && args.length == 2) {
      numBusinessAccounts = Integer.parseInt(args[0]);
      numPersonalAccounts = Integer.parseInt(args[1]);
    }

    // Create accouns with initial balance of 100
    Bank bank = new Bank(numBusinessAccounts, numPersonalAccounts, 100);
    bank.work();
    bank.printAllAccounts();


    // Check to see that all of the balances are stable
    for (int i=0; i < numBusinessAccounts+numPersonalAccounts; i++) {
      if (bank.getAccount(i).getBalance() != 300)
        everythingBalances = false;  // DK
    }

    // DK
    try {
      File output = new File("out.txt");
      FileWriter out = new FileWriter(output);

      if (everythingBalances) {
        out.write("< AccountSubType , finished , SUCCESS >" + "\r\n");
        System.out.println("< AccountSubType , finished , SUCCESS >");
      }
      else {
        out.write("< AccountSubType , finished , FAILURE >" + "\r\n");
        System.out.println("< AccountSubType , finished , FAILURE >");
      }
      out.close();
    }

    catch (Exception e) { // IOException  or  ClassNotFoundException
      System.out.println(e);
    }
  }    //end of function main
}      //end of class Main