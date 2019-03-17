class RWPrinter extends RWVSN {
  protected int reading = 0;

  protected void doRead() {
    // System.out.println("  enter doRead()");
    synchronized (this) {
      reading++;
    }
    // System.out.println("  reading = "+reading);
    if (RWVSNDriver.iw.x < 10)
      RWVSNDriver.iw.x++;

    // System.out.println("  exit doRead()");
    synchronized (this) {
      reading--;
    }
  }
  protected void doWrite() {
    if (reading > 0)
      throw new RuntimeException("bug found");

    if (RWVSNDriver.iw.x > 0)
      RWVSNDriver.iw.x--;
  }
}