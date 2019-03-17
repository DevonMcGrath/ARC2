import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * class Test: Used to test class AllocationVector.
 */
public class AllocTest {
  /**
   * Indicates number of threads runs to perform.
   */
  private static final int runsNum = 1;

  /**
  * MAIN METHOD.
  * Gets from command-line: 1. Name of output file.
  *                         2. Concurrency Parameter (little,average,lot).
  * @param args command-line arguments as written above.
  */

  public static void main(String[] args) {
    AllocationVector vector = null;
    TestThread1 Thread1 = null;
    TestThread1 Thread2 = null;
    int[] Thread1Result = null;
    int[] Thread2Result = null;
    FileOutputStream out = null;

    /**
     * Reading command-line arguments.
     */
    try {
      if (args.length != 2) {
        System.out.println("Error 1");  // DK
        throw new Exception();
      }



      // Opening output file with name 'args[0]' for append write.
    try
    {
      // DK: Inclusion of the second argument causes program to fail
      out = new FileOutputStream(args[0]);//, false);
    }
    catch (FileNotFoundException e)
    {
      System.out.println("Error 2");  // DK
      e.printStackTrace();
    }


      // Checking concurrency parameter correctness.
      if ( (args[1].compareTo("little") != 0) &&
           (args[1].compareTo("average") != 0) &&
           (args[1].compareTo("lot") != 0) ) {
        System.out.println("Error 3");  // DK
        throw new Exception();
      }


    } catch (Exception e) {
        System.err.println("Invalid command-line arguments...");
        System.exit(1);
    }

    /**
     * If here, then command-line arguments are correct.
     * Therefore, proceeding according to the concurrency parameter value.
     */
    // Setting threads run configuration according to concurrency parameter.
    if (args[1].compareTo("little") == 0) {
      // DK: Was 20000/1000/1000
      vector = new AllocationVector(2000);
      Thread1Result = new int[100];
      Thread2Result = new int[100];
    } else if (args[1].compareTo("lot") == 0) {
      // DK: Was 5000/5000/5000
      vector = new AllocationVector(500);
      Thread1Result = new int[500];
      Thread2Result = new int[500];
    } else if (args[1].compareTo("average") == 0) {
      // DK: Was 10000/2000/2000
      vector = new AllocationVector(1000);
      Thread1Result = new int[200];
      Thread2Result = new int[200];
    }

    // Creating threads, starting their run and waiting till they finish.
    Thread1 = new TestThread1(vector,Thread1Result);
    Thread2 = new TestThread1(vector,Thread2Result);

    Thread1.start();
    for (int i = 0; i < 100000; i++); // "Pause" between threads run to try "hide"
                                      // the BUG.
    Thread2.start();

    try {
      Thread1.join();
      Thread2.join();
    } catch (InterruptedException e) {
      System.err.println("Error joining threads...");
      System.exit(1);
    }

    // Checking correctness of threads run results and printing the according
    // tuple to output file.
    try {
     if (Thread1Result[0] == -2) {
       System.out.println("<FAILURE: Test, Thread1 tried to allocate block which is allocated, weak-reality (Two stage access)>\n");
       out.write("<FAILURE: Test, Thread1 tried to allocate block which is allocated, weak-reality (Two stage access)>\n".getBytes());
     } else if (Thread1Result[0] == -3){
       System.out.println("<FAILURE: Test, Thread1 tried to free block which is free, weak-reality (Two stage access)>\n");
       out.write("<FAILURE: Test, Thread1 tried to free block which is free, weak-reality (Two stage access)>\n".getBytes());
     } else if (Thread2Result[0] == -2) {
       System.out.println("<FAILURE: Test, Thread2 tried to allocate block which is allocated, weak-reality (Two stage access)>\n");
       out.write("<FAILURE: Test, Thread2 tried to allocate block which is allocated, weak-reality (Two stage access)>\n".getBytes());
     } else if (Thread2Result[0] == -3){
       System.out.println("<FAILURE: Test, Thread2 tried to free block which is free, weak-reality (Two stage access)>\n");
       out.write("<FAILURE: Test, Thread2 tried to free block which is free, weak-reality (Two stage access)>\n".getBytes());
     } else {
         //OperatedCorrectly = true;
        System.out.println("<SUCCESS: Test, correct-run, none>\n");
         out.write("<SUCCESS: Test, correct-run, none>\n".getBytes());
     }
   } catch (IOException ex) {
       //System.err.println("Error writing to output file...");
       //System.exit(1);
   }
  }
}
