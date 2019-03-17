import junit.framework.*;
import java.io.*;
import java.util.regex.*;

public class BufferTest extends TestCase {

  public static Test suite() {
  	return new TestSuite(BufferTest.class);
  }

  public void testBufferLot() throws Exception {
    BufferNotify AB = new BufferNotify();
    String[] input = new String[2];
    input[0] = "out.txt";
    input[1] = "3";
    AB.main(input);

    try{
      File file = new File("out.txt");
      byte[] bytes = new byte[(int) file.length()];
      FileInputStream fstream = new FileInputStream(file);
      fstream.read(bytes);
      fstream.close();

      String testOutput = new String(bytes);
      //System.out.println("Test output: " + testOutput);
      Pattern regex = Pattern.compile("Programs succesfuly excecuted,none");
      Matcher intMatch = regex.matcher(testOutput);
      assertTrue(intMatch.find());

    } catch (Exception e){
      System.err.println("testBufferLot error: " + e.getMessage());
    }
  }
}
