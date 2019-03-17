/**
 *@author Golan
 * 17/10/2003
 * 11:59:43
 *@version 1.0
 */

import java.io.*;

/**
 *
 */
public class ProgramRunner {

    private BuggedProgram bug;
    private DataOutputStream out;
    private int threadsNumber;

    /**
     *
     * @param threadsNumber
     */

    public ProgramRunner() {
    }

    public ProgramRunner(DataOutputStream output, int threadsNumber) {
        this.out = output;
        this.threadsNumber = threadsNumber;
        this.bug = new BuggedProgram(output, threadsNumber);
    }


    public void doWork() {
        String newLine = System.getProperty("line.separator");
        try {
            out.writeBytes("Number Of Threads: " + this.threadsNumber
              + " Number Of Bugs: ");
        }
        catch (IOException e) {
            e.printStackTrace(System.err);
        }
        this.bug.doWork();
    }


    public static void main(String[] args) {
        //File output = new File("output.txt");

        DataOutputStream out = null;
        try {
            FileOutputStream os = new FileOutputStream(args[0]);
            out = new DataOutputStream(os);
        } catch (FileNotFoundException e) {
            e.printStackTrace(System.err);
        }
        try {

            String newLine = System.getProperty("line.separator");
            out.writeBytes("In this file you will find the number of the bug appearances " +
                    "accordingly to the number of threads that the " +
                    "bugged program utilized with:" + newLine + newLine);

            if (args[1].equalsIgnoreCase("little"))
            {
              out.writeBytes(newLine + "************************************"
                + newLine + newLine);
              out.writeBytes("Few Threads: " + newLine + newLine);
              // DK: Was 17
              ProgramRunner fewThreads = new ProgramRunner(out, 7);
              fewThreads.doWork();
            }
            else if (args[1].equalsIgnoreCase("average"))
            {
              out.writeBytes(newLine + "************************************"
                + newLine + newLine);
              out.writeBytes("Average Threads: " + newLine + newLine);
              // DK: Was 40
              ProgramRunner averageThreads = new ProgramRunner(out, 10);
              averageThreads.doWork();
            }
            else
            {
              out.writeBytes(newLine + "************************************"
                + newLine + newLine);
              out.writeBytes("A Lot Of Threads: " + newLine + newLine);
              // DK: Was 120
              ProgramRunner aLotOfThreads = new ProgramRunner(out, 20);
              aLotOfThreads.doWork();
            }


        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        // If we reach this point, something has gone wrong
        //return false;


    }


}
