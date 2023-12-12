import io.swagger.client.*;
import io.swagger.client.model.AlbumsProfile;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientDemo {

    private static final int INITIAL_THREAD_POOL_SIZE = 10;
    private static final int INITIAL_GET_API_COUNT = 100;
    private static final int TEST_NUM_API_REQUEST = 100;
    private static final int TEST_NUM_GET_REVIEW_REQUEST = 1000;
    private static final String TEST_GET_ALBUM_ID = "6571fd5817a96c468bd8ad3a";
    private static final File TEST_POST_IMAGE_FILE = new File("src/nmtb.png");
    private static final AlbumsProfile TEST_POST_PROFILE = new AlbumsProfile();
    private static final RequestCounter counter = new RequestCounter();
    private static RequestRecordWriter writer = new RequestRecordWriter("./client_result.csv");

    public static void main(String[] args) throws InterruptedException, ApiException {
        int threadGroupSize, numThreadGroups, delay;
        String postIpAddress;
        String getIpAddress;
        TEST_POST_PROFILE.setArtist("Taylor Swift");
        TEST_POST_PROFILE.setYear("2019");
        TEST_POST_PROFILE.setTitle("Lover");
        if (args.length != 5) {
//            System.out.println("Usage: ClientDemo <threadGroupSize> <numThreadGroups> <delay> <ipAddress>");
//            return;
            threadGroupSize = Integer.parseInt("10");
            numThreadGroups = Integer.parseInt("10");
            delay = Integer.parseInt("2");
            postIpAddress ="http://localhost:8080";
            getIpAddress = "http://localhost:8080";
        } else {
            threadGroupSize = Integer.parseInt(args[0]);
            numThreadGroups = Integer.parseInt(args[1]);
            delay = Integer.parseInt(args[2]);
            postIpAddress = args[3];
            getIpAddress = args[4];
        }

        // Initialization phase
        CountDownLatch countDownLatchInitialization = new CountDownLatch(INITIAL_THREAD_POOL_SIZE);
        addThreadGroup(INITIAL_THREAD_POOL_SIZE, INITIAL_GET_API_COUNT, postIpAddress, countDownLatchInitialization);
        countDownLatchInitialization.await();

        // Working phase
        long start = System.currentTimeMillis();
        CountDownLatch getReviewsCountDownLatch = new CountDownLatch(3);
        GetReviewThread getReviewThread1 = new GetReviewThread(TEST_NUM_GET_REVIEW_REQUEST, getIpAddress, counter, getReviewsCountDownLatch, writer);
        GetReviewThread getReviewThread2 = new GetReviewThread(TEST_NUM_GET_REVIEW_REQUEST, getIpAddress, counter, getReviewsCountDownLatch, writer);
        GetReviewThread getReviewThread3 = new GetReviewThread(TEST_NUM_GET_REVIEW_REQUEST, getIpAddress, counter, getReviewsCountDownLatch, writer);
        // Start first thread group
        CountDownLatch countDownLatch = new CountDownLatch(threadGroupSize);
        addThreadGroup(threadGroupSize, TEST_NUM_API_REQUEST, postIpAddress, counter, countDownLatch, writer);
        TimeUnit.SECONDS.sleep(delay);
        // Wait until first thread group completed
        countDownLatch.await();
        // Start 3 get review threads
        long getStart = System.currentTimeMillis();
        new Thread(getReviewThread1).start();
        new Thread(getReviewThread2).start();
        new Thread(getReviewThread3).start();
        // Continue the following thread groups
        countDownLatch = new CountDownLatch(threadGroupSize * (numThreadGroups - 1));
        for (int i = 0; i < numThreadGroups - 1; i++) {
            addThreadGroup(threadGroupSize, TEST_NUM_API_REQUEST, postIpAddress, counter, countDownLatch, writer);
            TimeUnit.SECONDS.sleep(delay);
        }

        countDownLatch.await();
        // Stop get review threads
        getReviewThread1.terminate();
        getReviewThread2.terminate();
        getReviewThread3.terminate();
        getReviewsCountDownLatch.await();
        long end = System.currentTimeMillis();

        // Calculation and output
        long postWallTime = end - start;
        long getWallTime = end - getStart;
        int failedRequests = counter.getFailRequest();
        int successRequests = counter.getSuccessRequest();
        int failedGetReviewRequests = counter.getFailGetReviewRequest();
        int successGetReviewRequests = counter.getSuccessGetReviewRequest();
        int numPostRequests = failedRequests + successRequests;
        int numGetRequests = failedGetReviewRequests + successGetReviewRequests;
        long getThroughput = 1000 * numGetRequests / getWallTime;
        long postThroughput = 1000 * numPostRequests / postWallTime;
        System.out.println("Result for POST requests:\n"
                + "Number of failed requests: " + failedRequests + "\n"
                + "Number of successful requests: " + successRequests + "\n"
                + "Walltime (seconds): " + postWallTime / 1000 + "\n"
                + "Throughput (per second): " + postThroughput + "\n");

        System.out.println("Result for GET requests:\n"
                + "Number of failed requests: " + failedGetReviewRequests + "\n"
                + "Number of successful requests: " + successGetReviewRequests + "\n"
                + "Walltime (seconds): " + getWallTime / 1000 + "\n"
                + "Throughput (per second): " + getThroughput + "\n"
                + "------------------------");
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