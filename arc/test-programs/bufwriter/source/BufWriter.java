/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:
 * @author
 * @version 1.0
 */
import java.io.*;
import java.util.*;

public class BufWriter extends Thread {

  public static void main (String[] args)
  {
    Buffer buf = new Buffer(1000);
    File outFile;
    Thread[] wr;
    int threadNum = 5,res;
    Checker checker = new Checker (buf);
    Thread tCheck = new Thread(checker);
    FileOutputStream outF;
    Random rGen = new Random();
    double noSyncProbability = 0.1;

//Output file creation
    //if (args.length > 0)
    if (args.length == 0)   // DK
      System.exit(9);

    outFile = new File(args[0]);  // DK
    //else return true;

//Optional concurrency parameter
    if (args.length > 1)
    {
      if (args[1].equalsIgnoreCase("little")) threadNum = 3;
      if (args[1].equalsIgnoreCase("average")) threadNum = 5;
      if (args[1].equalsIgnoreCase("lot")) threadNum = 10;
    }

//Thread array creation
    wr = new Thread [threadNum];

//Threads creation
    for (int i=0;i<threadNum;i++)
    {
      // DK: Show bug
      //if (rGen.nextDouble() > noSyncProbability)
      //  wr[i] = new Thread(new SyncWriter(buf,i+1));
      //else
        wr[i] = new Thread(new Writer(buf,i+1));
    }

// Checker thread starting
    tCheck.start();

    outF = null; // DK
//Output stream creation
    try{
      outF = new FileOutputStream(outFile);
    }
    catch (Exception e) {System.exit(10);}

    DataOutputStream outStream = new DataOutputStream(outF);


//Starting threads ...
    for (int i=0;i<threadNum;i++)
      wr[i].start();

//Let them work ...
    try {
     sleep (10);
    }
    catch (InterruptedException e) {}

//Stopping threads ...
    for (int i=0;i<threadNum;i++)
      wr[i].stop();

//Stopping checker thread
    tCheck.stop();

    //boolean OperatedCorrectly = false; // DK
//Outputting results ...
    try
    {
      res = buf._count - (checker.getWrittenCount()+buf._pos);
      System.out.print("<BufWriter,");
      outStream.writeChars("<BufWriter,");
      outStream.writeChars(res+",");
      if (res != 0){
        System.out.print("[Wrong/No-Lock]>\n");
        outStream.writeChars("[Wrong/No-Lock]>");
      }
      else{
        //OperatedCorrectly = true;
        System.out.print("[None, worked correctly]>\n");
        outStream.writeChars("[None, worked correctly]>");
      }
      outStream.close();
    }
    catch (IOException e) {}
    //return OperatedCorrectly;
  }
}
