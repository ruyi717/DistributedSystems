package io.swagger.client.api;

public class RequestCounterBarrier {
  final static private int NUMTHREADS = 1000;
  private int count = 0;

  synchronized public void inc() {
    count++;
  }

  public int getVal() {
    return this.count;
  }
}
