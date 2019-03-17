import junit.framework.*;
import java.io.*;
import java.util.regex.*;

public class AirlineTest extends TestCase {

  public static Test suite() {
  	return new TestSuite(AirlineTest.class);
  }

  public void testAirlineLot() throws Exception {
    AirlineBug AB = new AirlineBug();
    //boolean result = false;
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
      Pattern regex = Pattern.compile("SUCCESS");
      Matcher intMatch = regex.matcher(testOutput);
      assertTrue(intMatch.find());

    } catch (Exception e){
      System.err.println("AccountsTest error: " + e.getMessage());
    }
  }
}
