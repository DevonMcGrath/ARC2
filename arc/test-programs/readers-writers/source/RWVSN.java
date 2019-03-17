abstract class RWVSN {

  //@ invariant !(activeReaders > 0 && activeWriters > 0);
  //@ invariant !(activeWriters > 1);

  protected int activeReaders = 0;  // threads executing read
  protected int activeWriters = 0;  // always zero or one

  protected int waitingReaders = 0; // threads not yet in read
  protected int waitingWriters = 0; // same for write

  protected abstract void doRead(); // implement in subclasses
  protected abstract void doWrite();

  public void read() /*throws InterruptedException*/ {
    beforeRead();
    try {
      doRead();
    } finally {
      afterRead();
    }
  }

  public void write() /*throws InterruptedException*/ {
    beforeWrite();
    try {
      doWrite();
    } finally {
      afterWrite();
    }
  }

  /*@ behavior
     @    ensures \result <==> (waitingWriters == 0 && activeWriters == 0);
     @*/
  protected /*@ pure @*/ boolean allowReader() {
    return waitingWriters == 0 && activeWriters == 0;
  }

  /*@ behavior
     @    ensures \result <==> (activeWriters == 0 && activeReaders == 0);
     @*/
  protected boolean allowWriter() {
    return activeReaders == 0 && activeWriters == 0;
  }

  /*@ behavior
     @    assignable waitingReaders, activeReaders;
     @    ensures activeReaders >= 1 && \old(allowReader()) ==> \old(activeReaders) + 1 == activeReaders;
     @*/
  protected void beforeRead() /*throws InterruptedException*/ {
    //synchronized(this) {
    try { // DK
      ++waitingReaders;
      while (!allowReader()) {
        try {
          wait();
        } catch (InterruptedException ie) {
          --waitingReaders; // roll back state
          //      throw ie;
        }  // catch
      }    // while
      --waitingReaders;
    } catch (Exception e) {
      RWVSNDriver.goodRun = false;
    }
    //}      // synch(this)
    // BUG: shrinking synch region exposes update bug
    //++activeReaders;
    //JPF POR workaround
    //int tmp = activeReaders;
    //tmp++;
    //activeReaders = tmp;
  }

  /*@ behavior
     @    assignable activeReaders;
     @    ensures \old(activeReaders) - 1 == activeReaders;
     @*/
  protected void afterRead()  {
    synchronized(this) {
      --activeReaders;
      notifyAll();
    }
  }

  /*@ behavior
     @    assignable waitingWriters, activeWriters;
     @    ensures activeWriters == 1 && \old(allowWriter()) ==> \old(activeWriters) + 1 == activeWriters;
     @*/
  protected void beforeWrite() /*throws InterruptedException*/ {
    synchronized(this) {
      ++waitingWriters;
      while (!allowWriter()) {
        try {
          wait();
        } catch (InterruptedException ie) {
          --waitingWriters;
          //      throw ie;
        } // catch
      }   // while
      --waitingWriters;
      ++activeWriters;
    }
  }

  /*@ behavior
     @    assignable activeWriters;
     @    ensures \old(activeWriters) - 1 == activeWriters;
     @*/
  protected void afterWrite() {
    synchronized(this) {
      --activeWriters;
      notifyAll();
    }
  }
}
