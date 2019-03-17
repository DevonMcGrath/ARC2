import junit.framework.*;
import java.io.*;
import java.util.regex.*;

public class LinkedListTest extends TestCase {

  public static Test suite() {
  	return new TestSuite(LinkedListTest.class);
  }

  public void testLinkedList() throws Exception {
    boolean goodRun = true;
    BugTester BT = new BugTester();
    String[] input = new String[2];
    input[0] = "2";
    input[1] = "20";

    try {
      BT.main(input);
    } catch (Exception e) {
      goodRun = false;
    }

    if (goodRun)
      System.out.println("It was a good run.");
    else
      System.out.println("It was a bad run.");

    assertTrue(goodRun);
  } // testLinkedList
}   // LinkedListTest
