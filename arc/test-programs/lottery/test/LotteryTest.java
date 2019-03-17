import junit.framework.*;
import java.io.*;
import java.util.regex.*;

public class LotteryTest extends TestCase {

  public static Test suite() {
  	return new TestSuite(LotteryTest.class);
  }

  public void testLotteryLot() throws Exception {
    //LotteryTest AB = new LotteryTest();
    String[] input = new String[2];
    input[0] = "out.txt";
    input[1] = "average";
    BuggyProgram.main(input);

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
      System.err.println("testAccount error: " + e.getMessage());
    }
  }
}
