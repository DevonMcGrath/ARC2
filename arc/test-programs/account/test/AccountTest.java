import junit.framework.*;
import java.io.*;
import java.util.regex.*;

public class AccountTest extends TestCase {

  public static Test suite() {
  	return new TestSuite(AccountTest.class);
  }

  public void testAccountLot() throws Exception {
    Main M = new Main();
    String[] input = new String[2];
    input[0] = "out.txt";
    input[1] = "average";
    M.main(input);

    try{
      File file = new File("out.txt");
      byte[] bytes = new byte[(int) file.length()];
      FileInputStream fstream = new FileInputStream(file);
      fstream.read(bytes);
      fstream.close();

      String testOutput = new String(bytes);
      Pattern regex = Pattern.compile("SUCCESS");
      Matcher intMatch = regex.matcher(testOutput);
      assertTrue(intMatch.find());

    } catch (Exception e){
      System.err.println("testAccount error: " + e.getMessage());
    }
  }
}
