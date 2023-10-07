import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CostOfFileAccessImproved {
    private static final int NUMTHREADS = 500;
    private static final int NUMSTRINGS = 1000;
    private static final String FILENAME = "output.txt";

    public static void main(String[] args) {
        PriorityQueue<String> stringQueue = new PriorityQueue<>(NUMTHREADS * NUMSTRINGS, (s1, s2) -> {
            // Comparator to order strings by timestamp
            long timestamp1 = Long.parseLong(s1.split(",")[0].trim());
            long timestamp2 = Long.parseLong(s2.split(",")[0].trim());
            return Long.compare(timestamp1, timestamp2);
        });

        Thread[] threads = new Thread[NUMTHREADS];
        FileWriterThread writerThread = new FileWriterThread(FILENAME, stringQueue);

        // Start the writer thread to handle file writing
        writerThread.start();

        for (int i = 0; i < NUMTHREADS; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    generateStrings(stringQueue);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Signal the writer thread to finish writing and exit
        writerThread.finish();
    }

    private static void generateStrings(PriorityQueue<String> stringQueue) {
        for (int i = 0; i < NUMSTRINGS; i++) {
            long timestamp = System.currentTimeMillis();
            long id = Thread.currentThread().getId();
            String line = timestamp + ", " + id + ", " + i;
            stringQueue.add(line);
        }
    }
}

class FileWriterThread extends Thread {
    private final String filename;
    private final PriorityQueue<String> queue;
    private volatile boolean running = true;
    private final Lock lock = new ReentrantLock();

    public FileWriterThread(String filename, PriorityQueue<String> queue) {
        this.filename = filename;
        this.queue = queue;
    }

    @Override
    public void run() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            while (running || !queue.isEmpty()) {
                lock.lock();
                try {
                    String line = queue.poll();
                    if (line != null) {
                        writer.write(line + "\n");
                    }
                } finally {
                    lock.unlock();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void finish() {
        running = false;
    }
}

