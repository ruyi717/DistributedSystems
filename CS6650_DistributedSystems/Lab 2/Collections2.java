import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.text.Style;

public class Collections2 {
    private static Integer ADDELEMENTS = 100000;
    private static Integer NUMTHREADS = 100;
    public static void main(String[] args) {
        // Hashtable的方法是Synchronize的，而HashMap不是，在多个线程访问Hashtable时，不需要自己为它的方法实现同步，而HashMap就必须为之提供外同步。
        singleThreaded(new Hashtable<>());
        singleThreaded(new HashMap<>());
        singleThreaded(new ConcurrentHashMap<>());

        multiThreaded(new Hashtable<>());
        multiThreaded(Collections.synchronizedMap(new HashMap<>()));
        multiThreaded(new ConcurrentHashMap<>());
    }

    private static void singleThreaded(Map<Integer, Integer> map) {
        long startTime = System.currentTimeMillis();

        for(int i = 0; i < ADDELEMENTS; i++) {
            map.put(i, i);
        }

        long endTime = System.currentTimeMillis();
        System.out.println(map.getClass().getSimpleName() + " single threaded time taken: " + (endTime - startTime) + " ms");
    }

    private static void multiThreaded(Map<Integer, Integer> map) {
        Thread[] threads = new Thread[NUMTHREADS];
        long startTime = System.currentTimeMillis();

        for(int i = 0; i < NUMTHREADS; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    addElementsToMap(map);
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

        long endTime = System.currentTimeMillis();
        System.out.println(map.getClass().getSimpleName() + " multi-threaded time taken: " + (endTime - startTime) + " ms");
    }

    private static void addElementsToMap(Map<Integer, Integer> map) {
        for (int j = 0; j < ADDELEMENTS / NUMTHREADS; j++) {
            map.put(j, j);
        }
    }
}
