import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CostOfFileAccess {
    private static Integer NUMTHREADS = 500;
    private static Integer NUMSTRINGS = 1000;
    private static final String FILENAME = "output.txt";
    public static void main(String[] args) {
        Thread[] threads = new Thread[NUMTHREADS];
        ConcurrentLinkedQueue<String> list = new ConcurrentLinkedQueue<>();

        for(int i = 0; i < NUMTHREADS; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    generateStrings(list);
                }
            });
            threads[i].start();
        }

        for(Thread thread: threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILENAME));
            for(String line: list) {
                writer.write(line);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateStrings(ConcurrentLinkedQueue<String> list) {
        for(int i = 0; i < NUMSTRINGS; i++) {
            long timestamp = System.currentTimeMillis();
            long id = Thread.currentThread().getId();
            String line = timestamp + ", " + id + ", " + i + "\n";
            list.add(line);
        }
    }
}
