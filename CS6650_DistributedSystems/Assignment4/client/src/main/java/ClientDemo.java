import io.swagger.client.*;
import io.swagger.client.model.AlbumsProfile;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientDemo {

    private static final int INITIAL_THREAD_POOL_SIZE = 10;
    private static final int INITIAL_GET_API_COUNT = 100;
    private static final int TEST_NUM_API_REQUEST = 100;
    private static final String TEST_GET_ALBUM_ID = "653e1c770668e525ac7ee620";
    private static final File TEST_POST_IMAGE_FILE = new File("src/nmtb.png");
    private static final AlbumsProfile TEST_POST_PROFILE = new AlbumsProfile();
    private static final RequestCounter counter = new RequestCounter();
    private static RequestRecordWriter writer = new RequestRecordWriter("./client_result.csv");

    public static void main(String[] args) throws InterruptedException, ApiException {
        int threadGroupSize, numThreadGroups, delay;
        String ipAddress;
        TEST_POST_PROFILE.setArtist("Taylor Swift");
        TEST_POST_PROFILE.setYear("2019");
        TEST_POST_PROFILE.setTitle("Lover");
        if (args.length != 4) {
//            System.out.println("Usage: ClientDemo <threadGroupSize> <numThreadGroups> <delay> <ipAddress>");
//            return;
            threadGroupSize = Integer.parseInt("10");
            numThreadGroups = Integer.parseInt("10");
            delay = Integer.parseInt("2");
            ipAddress ="http://localhost:8080";
        } else {
            threadGroupSize = Integer.parseInt(args[0]);
            numThreadGroups = Integer.parseInt(args[1]);
            delay = Integer.parseInt(args[2]);
            ipAddress = args[3];
        }

        // Initialization phase
        CountDownLatch countDownLatchInitialization = new CountDownLatch(INITIAL_THREAD_POOL_SIZE);
        addThreadGroup(INITIAL_THREAD_POOL_SIZE, INITIAL_GET_API_COUNT, ipAddress, countDownLatchInitialization);
        countDownLatchInitialization.await();

        // Working phase
        long start = System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(threadGroupSize * numThreadGroups);
        for (int i = 0; i < numThreadGroups; i++) {
            addThreadGroup(threadGroupSize, TEST_NUM_API_REQUEST, ipAddress, counter, countDownLatch, writer);
            TimeUnit.SECONDS.sleep(delay);
        }
        countDownLatch.await();
        long end = System.currentTimeMillis();

        // Calculation and output
        long wallTime = end - start;
        int failedRequests = counter.getFailRequest();
        int successRequests = counter.getSuccessRequest();
        int numRequests = failedRequests + successRequests;
        long throughput = 1000 * numRequests / wallTime;
        System.out.println("Result:\n"
                + "Number of failed requests: " + failedRequests + "\n"
                + "Number of successful requests: " + successRequests + "\n"
                + "Walltime (seconds): " + wallTime / 1000 + "\n"
                + "Throughput (per second): " + throughput);
        writer.calculateMetrics();
    }

    private static void addThreadGroup(int threadGroupSize, int numRequests, String ipAddress, CountDownLatch countDownLatch) {
        for (int i = 0; i < threadGroupSize; i++) {
            Thread clientThread = new Thread(new ClientThread(numRequests, ipAddress, countDownLatch, TEST_GET_ALBUM_ID, TEST_POST_IMAGE_FILE, TEST_POST_PROFILE));
            clientThread.start();
        }
    }

    private static void addThreadGroup(int threadGroupSize, int numRequests, String ipAddress, RequestCounter counter, CountDownLatch countDownLatch, RequestRecordWriter writer) {
        for (int i = 0; i < threadGroupSize; i++) {
            Thread clientThread = new Thread(new ClientThread(numRequests, ipAddress, counter, countDownLatch, TEST_GET_ALBUM_ID, TEST_POST_IMAGE_FILE, TEST_POST_PROFILE, writer));
            clientThread.start();
        }
    }
}