import junit.framework.*;
import java.io.*;
import java.util.regex.*;

public class AccountSubTypeTest extends TestCase {

  public static Test suite() {
  	return new TestSuite(AccountSubTypeTest.class);
  }

  public void testAccountSubType() throws Exception {
    try{
      File delFile = new File("out.txt");
      if (delFile.exists()) {
        delFile.delete();
      }
    } catch (Exception e){
      System.err.println("AccountSubTypeTest file delete error: " + e.getMessage());
    }

    Main M = new Main();
    String[] input = new String[2];
    input[0] = "1";
    input[1] = "10";
    M.main(input);

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
      System.err.println("AccountSubTypeTest error: " + e.getMessage());
    }
  } // testAccountSubType
}   // AccountSubTypeTest
