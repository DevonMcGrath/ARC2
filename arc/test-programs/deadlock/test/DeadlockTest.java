import junit.framework.*;
import java.io.*;
import java.util.regex.*;

public class DeadlockTest extends TestCase {

  public static Test suite() {
  	return new TestSuite(DeadlockTest.class);
  }

  public void testDeadlockLot() throws Exception {
    deadLock AB = new deadLock();
    String[] input = new String[2];
    input[0] = "out.txt";
    input[1] = "average";
    AB.main(input);

    try{
      File file = new File("out.txt");
      byte[] bytes = new byte[(int) file.length()];
      FileInputStream fstream = new FileInputStream(file);
      fstream.read(bytes);
      fstream.close();

      String testOutput = new String(bytes);
      Pattern regex = Pattern.compile("bug1,1,none");
      Matcher intMatch = regex.matcher(testOutput);
      assertTrue(intMatch.find());

    } catch (Exception e){
      System.err.println("testDeadlock error: " + e.getMessage());
    }
  }
}
