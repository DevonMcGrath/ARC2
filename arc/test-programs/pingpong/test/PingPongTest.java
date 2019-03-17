import junit.framework.*;
import java.io.*;
import java.util.regex.*;

public class PingPongTest extends TestCase {

  public static Test suite() {
  	return new TestSuite(PingPongTest.class);
  }

  public void testPingPongLot() throws Exception {
    ProgramRunner AB = new ProgramRunner();
    boolean result = false;
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
    Pattern regex = Pattern.compile("(\\d+)");
    Matcher intMatch = regex.matcher(testOutput);
    // We're interested in the second integer from the PingPong output
    // eg: Number Of Threads: 20 Number Of Bugs: 0 bugs.
    intMatch.find();
    intMatch.find();
    //System.out.println("intMatch.group(): '" + intMatch.group() + "'");
    assertTrue(intMatch.group().compareTo("0") == 0);

  } catch (Exception e){
    System.err.println("testPingPongLot error: " + e.getMessage());
  }


  } // testPingPongLot
}  // PingPongTest
