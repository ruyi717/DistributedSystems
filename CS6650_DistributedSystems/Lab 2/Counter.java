import java.util.concurrent.atomic.AtomicInteger;

// We wrap the atomicInteger in a class called Counter to encapsulate its behavior.
class Counter {
    // To implement a shared counter that multiple threads can increment safely, we use an AtomicInteger. 
    // An AtomicInteger is a thread-safe integer that supports atomic operations like incrementAndGet(). 

    private AtomicInteger value = new AtomicInteger(0);

    // To ensure that multiple threads do not interfere with each other while incrementing the shared counter, 
    //  we make use of the synchronized keyword in the increment method of the Counter class.
    // This ensures that only one thread can execute the increment method at a time.
    public synchronized void increment() {
        value.incrementAndGet();
    }

    public int getValue() {
        return value.get();
    }
}

// Each instance of this class represents a thread that increments the shared counter.
class IncrementThread extends Thread {
    private Counter counter;
    private int increments;

    public IncrementThread(Counter counter, int increments) {
        this.counter = counter;
        this.increments = increments;
    }

    @Override
    public void run() {
        for (int i = 0; i < increments; i++) {
            counter.increment();
        }
    }
}
