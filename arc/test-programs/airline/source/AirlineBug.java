
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: amit rotstein I.D: 037698867
 * Date: Oct 17, 2003
 * Time: 1:02:13 PM
 */
public class AirlineBug implements Runnable
{
  static int Num_Of_Seats_Sold = 0;
  int Maximum_Capacity, Num_of_tickets_issued;
  boolean StopSales = false;
  Thread threadArr[];
  FileOutputStream output;
  private String fileName;

  public void AirBug (String fileName, String Concurency)
  {
    this.fileName = fileName;

    Num_Of_Seats_Sold = 0; // DK

    try
    {
      output = new FileOutputStream(fileName);
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }

    // ****** Important
    // Reduced number of threads from 10/100/5000 to
    // 10/20/30 to make it tractable for JPF
    if(Concurency.equals("little"))
      Num_of_tickets_issued = 10;
    else if(Concurency.equals("average"))
      Num_of_tickets_issued = 20;
    else //(Concurency.equals( "lot"))
      Num_of_tickets_issued = 30;

    // issuing 10% more tickets for sale
    Maximum_Capacity = Num_of_tickets_issued - (Num_of_tickets_issued)/10 ;

    threadArr = new Thread[Num_of_tickets_issued];

    System.out.println( "The airline issued "+ Num_of_tickets_issued
      +" tickets for " +Maximum_Capacity+" seats to be sold.");
/**
 * starting the selling of the tickets:
 * "StopSales" indicates to the airline that the max capacity was sold &
 * that they should stop issuing tickets
 */
    for( int i=0; i < Num_of_tickets_issued; i++)
    {
      threadArr[i] = new Thread (this) ;
/**
 * first the airline is checking to see if it's agents had sold all the seats:
 */
      if( StopSales )
      {
        Num_Of_Seats_Sold--;
        break;
      }
 /**
 * THE BUG : StopSales is updated by the selling posts,
 * and by the time it is updated more tickets then are allowed to be are
 * sold by other threads that are still running
 */
      threadArr[i].start();  // "make the sale !!!"
    } // for
    System.out.println("SOLD "+ Num_Of_Seats_Sold + " Seats !!!");

    try
    {
      output = new FileOutputStream(fileName);
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }

    String str1="< SUCCESS! " +fileName+ " , Concurency="+Concurency+" , sold: " + Num_Of_Seats_Sold
      +" No Bug"+" >\n";
    String str2="< FAILURE! "+fileName+" , Concurency="+Concurency+" , sold: " + Num_Of_Seats_Sold
      +" Bug occurred"+" >\n";

    // Didn't work
    if (Num_Of_Seats_Sold > Maximum_Capacity)
      try
      {
        //OperatedCorrectly = false;
        System.out.println(str2);
        output.write(str2.getBytes());
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    else   // Worked
      try
      {
        //OperatedCorrectly = true;
        System.out.println(str1);
        output.write(str1.getBytes());
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }

    //return OperatedCorrectly;
  } // method bug
/**
 * the selling post:
 * making the sale & checking if limit was reached ( and updating "StopSales" ),
 */

  public void run()
  {
    Num_Of_Seats_Sold++;       // making the sale

    if (Num_Of_Seats_Sold > Maximum_Capacity)  // checking
      StopSales = true;      // updating
  }

  /**
  * Input arguments are:
  * 1. Output file name (eg: out.txt)
  * 2. One of (little, average, lot)
  */
  public static void main(String args[])
  {
    AirlineBug AB  = new AirlineBug();
    AB.AirBug(args[0],args[1]);
  }

}



