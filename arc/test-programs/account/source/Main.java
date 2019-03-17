import java.io.*;
import java.util.concurrent.*;

/**
 * Title:     Software Testing course
 * Description:  The goal of the exercise is implementing a  program which demonstrate  a parallel bug.
 * In the exercise we have two accounts.The program enable tranfering  money from one account to the other.Although the functions were defended by locks (synchronize) there exists an interleaving which we'll experience a bug.
 * Copyright:    Copyright (c) 2003
 * Company:   Haifa U.
 * @author Zoya Shaham and  Maya Maimon
 * @version 1.0
 */

public class Main
{

  public static void main(String[] args)
  {
    PrintStream out = null;

    try
    {
      if(args.length>0)
      {
        out=new PrintStream(new FileOutputStream(args[0]));
        // System.setOut(out);//directint all the "out" printing to the file.
        if(args.length==1) //the default value=little
          System.out.println("The default value=little");

        out.print("<Account program,");

        // ------- Begin argument handling
        if(args.length==2) //the concurrency is optional
        {
          String concurrencyLevel=args[1];
          if(concurrencyLevel.compareTo("little")==0)
          {
            ManageAccount.num=2;
            System.out.println("concurrency level = little");
          }
          else if(concurrencyLevel.compareTo("average")==0)
          {
            ManageAccount.num=5;
            System.out.println("concurrency level = average");
          }
          else if(concurrencyLevel.compareTo("lot")==0)
          {
            ManageAccount.num=10;
            System.out.println("concurrency level = lot");
          }
          else //wrong second argument
          {
            System.out.println("The (optional) second argument should be one of the following:");
	          System.out.println("(little, average or lot) of concurrency");
            System.exit(1);
          }
        } // If args.length == 2
        else //more than 2 arguments
        {
          if(args.length>2)
          {
            System.out.println("The program has two arguments: 1. Output file name and ");
            System.out.println("2. Optionally one of (little, average or lot) of concurrency");
           System.exit(1);
          }
        }  // if args.length > 2
      }    // if args.length > 0
      else //main's parameter is missing
      {
        System.out.println("The program requires an output file name and optionally");
        System.out.println("one of (little, average or lot) of concurrency");
        System.exit(1);
      }
      // ------------ End of argument handling

      System.out.println("The Initial values:");
      ManageAccount[] bank=new ManageAccount[ManageAccount.num];//2 is the default size
      String[] accountName={new String("A"),new String("B"),new String("C"),
        new String("D"),new String("E"), new String("F"),new String("G"),
        new String("H"),new String("I"),new String("J"),};

      for (int j=0;j<ManageAccount.num;j++)
      {
        bank[j]=new ManageAccount(accountName[j],100);
        ManageAccount.accounts[j].print();//print it
      }

      //start the threads
      for (int k=0;k<ManageAccount.num;k++)
        bank[k].start();

      // wait until all are finished
      for (int k=0;k<ManageAccount.num;k++)
        bank[k].join();

      System.out.println("The final values:");
      ManageAccount.printAllAccounts();

      //updating the output file
      boolean less=false,more=false;//flags which will indicate the kind of the bug

      for (int k=0;k<ManageAccount.num;k++)
      {
        if(ManageAccount.accounts[k].amount<300)
          less=true;
        else if(ManageAccount.accounts[k].amount>300)
          more=true;
      }


      // 3 Fail cases return false
      if((less==true)&&(more==true))
      {
        System.out.println("FAILURE!: > 300 case, < 300 case and no lock");
        out.print("FAILURE!: There is amount with more than 300 and there is amount");
        out.print("with less than 300, No Lock>");
        out.close();
      }
      else if((less==false)&&(more==true))
      {
        System.out.println("FAILURE!: > 300 case and no lock");
        out.print("FAILURE!: There is amount with more than 300, No Lock>");
        out.close();
      }
      else if((less==true)&&(more==false))
      {
        System.out.println("FAILURE!: < 300 case and no lock");
        out.print("FAILURE!: There is amount with less than 300, No Lock>");
        out.close();
      }
      // Pass case, return true
      else //if((less==false)&&(more==false))
      {
        System.out.println("SUCCESS!: All are 300");
        out.print("SUCCESS!: All amounts are 300,None>");
        out.close();
      }
    } // try
    catch(Exception e) //FileNotFound,Security
    {
      System.out.println(e);
      if (out!=null)
        out.close();
    }
  }//end of function main
}//end of class Main
