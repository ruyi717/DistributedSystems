package io.swagger.client.api;

//import com.squareup.okhttp.OkHttpClient;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.ImageMetaData;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.httpclient.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import okhttp3.OkHttpClient;


public class ServerLoadTester {

  public static void main(String[] args) throws InterruptedException, IOException {
    if (args.length != 4) {
      System.out.println(
          "Usage: java ServerLoadTester <threadGroupSize> <numThreadGroups> <delay> <IPAddr>");
      System.exit(1);
    }

    int threadGroupSize = Integer.parseInt(args[0]);
    int numThreadGroups = Integer.parseInt(args[1]);
    int delay = Integer.parseInt(args[2]);
    String postURI = args[3];
    String getURI = postURI + "/1";
    Map<Integer, List<LatencyRecord>> recordMap = new HashMap<>();

    AtomicReference<Integer> successfulRequests = new AtomicReference<>(0);
    AtomicReference<Integer> totalRequests = new AtomicReference<>(0);
    AtomicReference<Integer> failedRequests = new AtomicReference<>(0);
    CountDownLatch initialThreadsCompleted = new CountDownLatch(10);
    CountDownLatch completed = new CountDownLatch(threadGroupSize * numThreadGroups);

    List<LatencyRecord> latencyRecords = new ArrayList<>();
    final RequestCounterBarrier counter = new RequestCounterBarrier();
    DefaultApi apiInstance = new DefaultApi();

    for (int i = 0; i < 10; i++) {
      Runnable thread = () -> {
        for(int j = 0; j < 100; j++) {
          DefaultApiPost.main(postURI, apiInstance);
          DefaultApiGet.main(getURI, apiInstance);
        }
        counter.inc();
        initialThreadsCompleted.countDown();

      };
      new Thread(thread).start();
    }
    initialThreadsCompleted.await();

    long startTime = System.currentTimeMillis();
    for (int threadGroup = 0; threadGroup < numThreadGroups; threadGroup++) {
      for (int i = 0; i < threadGroupSize; i++) {
          Runnable threadN = () -> {
//          DefaultApi apiInstance = new DefaultApi();
          for (int j = 0; j < 1000; j++) {
            int retryCount = 0;
            int maxRetries = 5;
            boolean requestSuccessful = false;
            while (retryCount < maxRetries) {
              long requestStartTime = System.currentTimeMillis();
              try {
                ImageMetaData postResult = DefaultApiPost.main(postURI, apiInstance);
                long requestEndTime = System.currentTimeMillis();
                long latencyPost = requestEndTime - requestStartTime;

                requestStartTime = System.currentTimeMillis();
                AlbumInfo getResult = DefaultApiGet.main(getURI, apiInstance);
                requestEndTime = System.currentTimeMillis();

                if (getResult != null && postResult != null) {
                  recordLatency(recordMap, "POST", latencyPost, 201,1);
                  recordLatency(recordMap, "GET", requestEndTime - requestStartTime, 200,2);
                  successfulRequests.updateAndGet(v -> v + 2);
                } else if(getResult == null && postResult == null){
                  failedRequests.updateAndGet(v -> v + 2);
                } else if(getResult == null){
                  recordLatency(recordMap, "POST", latencyPost, 201,1);
                  successfulRequests.updateAndGet(v -> v+1);
                  failedRequests.updateAndGet(v -> v+1);
                } else {
                  recordLatency(recordMap, "GET", requestEndTime - requestStartTime, 200,2);
                  successfulRequests.updateAndGet(v -> v+1);
                  failedRequests.updateAndGet(v -> v+1);
                }
                totalRequests.updateAndGet(v -> v + 2);
                requestSuccessful = true;
                break;
              } catch (Exception e) {
                failedRequests.updateAndGet(v -> v+2);
              }
              if (requestSuccessful) {
                break;
              }
              retryCount++;
              if (retryCount < maxRetries) {
                try {
                  Thread.sleep(1000);
                } catch (InterruptedException ex) {
                  ex.printStackTrace();
                }
              }
//              catch (Exception e) {
//                failedRequests.updateAndGet(v -> v + 2);
//                totalRequests.updateAndGet(v -> v + 2);
//                retryCount++;
//                if (retryCount < maxRetries) {
//                  try {
//                    Thread.sleep(1000);
//                  } catch (InterruptedException ex) {
//                    ex.printStackTrace();
//                  }
//                }
//              }
            }
//            System.out.println("v: " + successfulRequests);
          }
          counter.inc();
          completed.countDown();
        };
          new Thread(threadN).start();
      }
      try {
        Thread.sleep(delay * 1000); // Sleep for 'delay' seconds between thread groups
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    completed.await();
    long endTime = System.currentTimeMillis();
    long wallTime = (endTime - startTime) / 1000;
    double throughput = (double) totalRequests.get() / wallTime;
//    // Write latency records to a CSV file
    writeLatencyRecordsToCSV(recordMap, "latency_records.csv");
//
//    // Calculate and display statistics
    calculateAndDisplayStatistics(recordMap);
    System.out.println("Wall Time: " + wallTime + " seconds");
    System.out.println("Throughput: " + throughput + " requests/second");
    System.out.println("Successful Requests: " + successfulRequests.get());
    System.out.println("Failed Requests: " + failedRequests.get());
  }

  private static void recordLatency(Map<Integer,List<LatencyRecord>> latencyRecords, String requestType, long latency, int responseCode, int key) {
    LatencyRecord record = new LatencyRecord(System.currentTimeMillis(), requestType, latency, responseCode);
    List<LatencyRecord> currList = latencyRecords.getOrDefault(key, new ArrayList<LatencyRecord>());
    currList.add(record);
    latencyRecords.put(key, currList);
  }

  private static void writeLatencyRecordsToCSV(Map<Integer,List<LatencyRecord>> records, String fileName) throws IOException {
    try (Writer writer = new FileWriter(fileName)) {
      writer.write("Start Time,Request Type,Latency (ms),Response Code\n");
      for (LatencyRecord record : records.get(1)) {
        writer.write(record.getCSVRecord() + "\n");
      }
      for (LatencyRecord record : records.get(2)) {
        writer.write(record.getCSVRecord() + "\n");
      }
    }
  }

  private static void calculateAndDisplayStatistics(Map<Integer,List<LatencyRecord>> records) {
    List<Long> latenciesPost = new ArrayList<>();
    List<Long> latenciesGet = new ArrayList<>();
    List<LatencyRecord> postRecords = records.get(1);
    List<LatencyRecord> getRecords = records.get(2);
    for (LatencyRecord record : postRecords) {
      latenciesPost.add(record.getLatency());
    }
    for (LatencyRecord record : getRecords) {
      latenciesGet.add(record.getLatency());
    }

    Collections.sort(latenciesPost);
    int size = latenciesPost.size();
    long min = latenciesPost.get(0);
    long max = latenciesPost.get(size - 1);
    long mean = latenciesPost.stream().mapToLong(Long::longValue).sum() / size;
    long median = size % 2 == 0 ? (latenciesPost.get(size / 2 - 1) + latenciesPost.get(size / 2)) / 2 : latenciesPost.get(size / 2);
    int p99Index = (int) Math.ceil(0.99 * size);
    long p99 = latenciesPost.get(p99Index - 1);

    System.out.println("Post Request: ");
    System.out.println("Min Latency: " + min + " ms");
    System.out.println("Max Latency: " + max + " ms");
    System.out.println("Mean Latency: " + mean + " ms");
    System.out.println("Median Latency: " + median + " ms");
    System.out.println("99th Percentile Latency (p99): " + p99 + " ms");

    Collections.sort(latenciesGet);
    size = latenciesGet.size();
    min = latenciesGet.get(0);
    max = latenciesGet.get(size - 1);
    mean = latenciesGet.stream().mapToLong(Long::longValue).sum() / size;
    median = size % 2 == 0 ? (latenciesGet.get(size / 2 - 1) + latenciesGet.get(size / 2)) / 2 : latenciesGet.get(size / 2);
    p99Index = (int) Math.ceil(0.99 * size);
    p99 = latenciesGet.get(p99Index - 1);

    System.out.println("Get Request: ");
    System.out.println("Min Latency: " + min+ " ms");
    System.out.println("Max Latency: " + max + " ms");
    System.out.println("Mean Latency: " + mean + " ms");
    System.out.println("Median Latency: " + median + " ms");
    System.out.println("99th Percentile Latency (p99): " + p99 + " ms");
  }
}
//    List<ClientMultiThreaded> threads = new ArrayList<>();
//    for (int threadGroup = 0; threadGroup < numThreadGroups; threadGroup++) {
//      for (int i = 0; i < threadGroupSize; i++) {
//        System.out.println("threadGroupSize i: " + i);
//        ClientMultiThreaded thread = new ClientMultiThreaded(getURI, serverURI);
//        thread.start();
//        threads.add(thread);
//        totalRequests.updateAndGet(v -> v + 2000);
//        try {
//          Thread.sleep(delay * 1000); // Sleep for 'delay' seconds between thread groups
//        } catch (InterruptedException e) {
//          e.printStackTrace();
//        }
//        System.out.println("finish threadGroupSize " + i);
//      }
//      System.out.println("finish threadGroup " + threadGroup);
//    }
//    for (ClientMultiThreaded thread : threads) {
//      thread.join();
//    }
//

//    try {
//      httpclient.close();
//    } catch (IOException e) {
//      throw new RuntimeException(e);
//    }
//  }

