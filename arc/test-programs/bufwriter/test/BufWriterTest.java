import junit.framework.*;
import java.io.*;
import java.util.regex.*;

public class BufWriterTest extends TestCase {

  public static Test suite() {
  	return new TestSuite(BufWriterTest.class);
  }

  public void testBufWriterLot() throws Exception {
    BufWriter AB = new BufWriter();
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
      Pattern regex = Pattern.compile("worked correctly");
      Matcher intMatch = regex.matcher(testOutput);
      assertTrue(intMatch.find());

    } catch (Exception e){
      System.err.println("testBufWriter error: " + e.getMessage());
    }
  }
}
