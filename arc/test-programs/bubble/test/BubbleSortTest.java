import junit.framework.*;
import java.io.*;
import java.util.regex.*;

public class BubbleSortTest extends TestCase {

  public static Test suite() {
  	return new TestSuite(BubbleSortTest.class);
  }

  public void testBubbleSortLot() throws Exception {
    SoftWareVerificationHW AB = new SoftWareVerificationHW();
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
      System.out.println("Test output: " + testOutput);
      Pattern regex = Pattern.compile("No bugs found");
      Matcher intMatch = regex.matcher(testOutput);
      assertTrue(intMatch.find());

    } catch (Exception e){
      System.err.println("testPingPongLot error: " + e.getMessage());
    }
  }
}
