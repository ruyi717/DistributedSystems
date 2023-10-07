
public class MultiThreadCounter {
    public static void main(String[] args) {
        int numThreads = 1; // Change this to the desired number of threads
        int incrementsPerThread = 10;
        Counter counter = new Counter();

        long startTime = System.currentTimeMillis();

        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new IncrementThread(counter, incrementsPerThread);
            // We start each thread, which will execute the run method of the IncrementThread class. Each thread will increment the shared counter 10 times.
            threads[i].start();
        }

        // Wait for all threads to finish

        /**
         * We use thread.join() to wait for all threads to finish before proceeding. This ensures that we don't print the counter value before all threads have completed their work.
         */
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Counter value: " + counter.getValue());
        System.out.println("Total execution time (ms): " + duration);
    }
}
