import junit.framework.*;
import java.io.*;
import java.util.regex.*;

public class Bubblesort2Test extends TestCase {

  public static Test suite() {
  	return new TestSuite(Bubblesort2Test.class);
  }

  public void testBubblesort2Lot() throws Exception {
    //Main M = new Main();
    String[] input = new String[2];
    input[0] = "out.txt";
    input[1] = "average";
    Loader.main(input);

    try{
      File file = new File("out.txt");
      byte[] bytes = new byte[(int) file.length()];
      FileInputStream fstream = new FileInputStream(file);
      fstream.read(bytes);
      fstream.close();

      String testOutput = new String(bytes);
      System.out.println("Test output: " + testOutput);
      Pattern regex = Pattern.compile("finished with No Bug");
      Matcher intMatch = regex.matcher(testOutput);
      assertTrue(intMatch.find());

    } catch (Exception e){
      System.err.println("testPingPongLot error: " + e.getMessage());
    }
  }
}
