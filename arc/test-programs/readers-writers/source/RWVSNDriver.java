public class RWVSNDriver {
	static RWPrinter rwp;
	static IntWrapper iw;
  static int bound; // if set to -1 then infinite loop
  static int reading;
  public static boolean goodRun = true; // DK

	public static void main(String argv[]) throws Exception {
		rwp = new RWPrinter();
		iw = new IntWrapper();

		int readers = 2;
		int writers = 2;
		bound = 100;
    reading = 0;

    // DK: JPF doesn't like Integer.parseInt
		//if((argv != null) && (argv.length == 3)) {
		//   readers = Integer.parseInt(argv[0]);
		//   writers = Integer.parseInt(argv[1]);
		//   bound = Integer.parseInt(argv[2]);
		//}

		for (int i = 0; i < readers; i++)
			new Reader(rwp, i).start();


		for (int i = 0; i < writers; i++)
			new Writer(rwp, i).start();

    if (goodRun == false)    // DK
      throw new Exception();

	}
}







