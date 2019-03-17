final class Writer extends Thread {
  protected RWPrinter rwp;
  protected int id;

  public Writer(RWPrinter r, int id) {
    rwp = r;
    this.id = id;
  }

  public void run() {
    for (int i = 0; i < RWVSNDriver.bound; i++)
       rwp.write();
  }
}