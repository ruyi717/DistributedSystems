import java.util.ArrayList;
import java.util.Vector;

public class Collections1 {
    public static void main(String[] args) {
        final int elementsToAdd = 100000;
        Vector<Integer> vector = new Vector<>();
        ArrayList<Integer> arrayList = new ArrayList<>();

        // Adding elements to Vector with synchronization
        long startTime = System.nanoTime();
        for (int i = 0; i < elementsToAdd; i++) {
            synchronized (vector) {
                vector.add(i);
            }
        }
        long endTime = System.nanoTime();
        long vectorTime = endTime - startTime;

        // Adding elements to ArrayList (no synchronization)
        startTime = System.nanoTime();
        for (int i = 0; i < elementsToAdd; i++) {
            arrayList.add(i);
        }
        endTime = System.nanoTime();
        long arrayListTime = endTime - startTime;

        System.out.println("Time taken to add " + elementsToAdd + " elements to Vector: " + vectorTime + " nanoseconds");
        System.out.println("Time taken to add " + elementsToAdd + " elements to ArrayList: " + arrayListTime + " nanoseconds");
    }
}
