import junit.framework.*;
import java.io.*;
import java.util.regex.*;

public class DiningPhilTest extends TestCase {

  public static Test suite() {
  	return new TestSuite(DiningPhilTest.class);
  }

  public void testDiningPhil() throws Exception {
    DiningPhil diners = new DiningPhil();
    String[] input = new String[1];
    input[0] = "6";
    diners.main(input);

    // try{
    //   File file = new File("out.txt");
    //   byte[] bytes = new byte[(int) file.length()];
    //   FileInputStream fstream = new FileInputStream(file);
    //   fstream.read(bytes);
    //   fstream.close();

    //   String testOutput = new String(bytes);
    //   Pattern regex = Pattern.compile("SUCCESS");
    //   Matcher intMatch = regex.matcher(testOutput);
    //   assertTrue(intMatch.find());

    // } catch (Exception e){
    //   System.err.println("testDiningPhil error: " + e.getMessage());
    // }
  }
}
