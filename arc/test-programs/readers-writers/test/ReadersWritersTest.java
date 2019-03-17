import junit.framework.*;
import java.io.*;
import java.util.regex.*;

public class ReadersWritersTest extends TestCase {

  public static Test suite() {
  	return new TestSuite(ReadersWritersTest.class);
  }

  public void testReadersWriters() throws Exception {
    boolean goodRun = true;

    RWVSNDriver RWD = new RWVSNDriver();
    String[] input = new String[3];
    input[0] = "100";
    input[1] = "100";
    input[2] = "100";

    try {
      RWD.main(input);
    } catch (Exception e) {
      goodRun = false;
    }

    if (goodRun)
      System.out.println("It was a good run.");
    else
      System.out.println("It was a bad run.");

    assertTrue(goodRun);

  } // testReadersWriters
}   // ReadersWritersTest
