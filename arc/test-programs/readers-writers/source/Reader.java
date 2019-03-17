final class Reader extends Thread {
  protected RWPrinter rwp;
  protected int id;

  public Reader(RWPrinter r, int id) {
    rwp = r;
    this.id = id;
  }

  public void run() {
    for (int i = 0; i < RWVSNDriver.bound; i++)
       rwp.read();
  }
}