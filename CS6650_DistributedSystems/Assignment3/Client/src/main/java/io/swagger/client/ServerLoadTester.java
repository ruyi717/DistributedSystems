package io.swagger.client;

import io.swagger.client.api.DefaultApi;
import io.swagger.client.api.LatencyRecord;
import io.swagger.client.api.LikeApi;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;


public class ServerLoadTester {
  private final static String FILEPATH = "/Users/jouy/desktop/DistributedSystems/CS6650_DistributedSystems/Assignment2/Assignment2_Client1/src/main/java/io/swagger/client/api/image.jpg";
  private final static String GET_ALBUMID = "6543233ef0009eeeebfb1dd4";


  public static void main(String[] args) throws InterruptedException, IOException {
    if (args.length != 4) {
      System.out.println(
          "Usage: java ServerLoadTester <threadGroupSize> <numThreadGroups> <delay> <IPAddr>");
      System.exit(1);
    }

    int threadGroupSize = Integer.parseInt(args[0]);
    int numThreadGroups = Integer.parseInt(args[1]);
    int delay = Integer.parseInt(args[2]);
    String ip = args[3];
    String postURI = ip + "/albums";

    Map<Integer, List<LatencyRecord>> recordMap = new HashMap<>();
    AtomicReference<Integer> successfulRequests = new AtomicReference<>(0);
    AtomicReference<Integer> totalRequests = new AtomicReference<>(0);
    AtomicReference<Integer> failedRequests = new AtomicReference<>(0);
    CountDownLatch completed = new CountDownLatch(threadGroupSize * numThreadGroups);

    List<LatencyRecord> latencyRecords = new ArrayList<>();
    final RequestCounterBarrier counter = new RequestCounterBarrier();
    DefaultApi apiInstance = new DefaultApi();
    LikeApi likeApiInstance = new LikeApi();
    likeApiInstance.getApiClient().setBasePath(ip);

    long startTime = System.currentTimeMillis();
    for (int threadGroup = 0; threadGroup < numThreadGroups; threadGroup++) {
      for (int i = 0; i < threadGroupSize; i++) {
          Runnable threadN = () -> {
          for (int j = 0; j < 100; j++) {
            int retryCount = 0;
            int maxRetries = 5;
            String id = "";
            boolean requestSuccessful = false;
            while (retryCount < maxRetries) {
              long requestStartTime = System.currentTimeMillis();
              try {
                File image = new File(FILEPATH);
                AlbumsProfile profile = new AlbumsProfile();// AlbumsProfile |
                profile.setArtist("Faye Wang");
                profile.setTitle("Dream");
                profile.setYear("1999");
                ApiResponse<ImageMetaData> postResult = apiInstance.newAlbumWithHttpInfo(image, profile, postURI);
                long requestEndTime = System.currentTimeMillis();
                long latencyPost = requestEndTime - requestStartTime;
                if (postResult != null) {
                  recordLatency(recordMap, "POST", latencyPost, postResult.getStatusCode(),1);
                  successfulRequests.updateAndGet(v -> v + 1);
                } else {
                  failedRequests.updateAndGet(v -> v+1);
                }
                totalRequests.updateAndGet(v -> v + 1);
                requestSuccessful = true;
                id = postResult.getData().getAlbumID();
                break;
              } catch (Exception e) {
                failedRequests.updateAndGet(v -> v+1);
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
            }
            //Like request
            retryCount = 0;
            maxRetries = 5;
            requestSuccessful = false;
            for(int count = 0; count < 2; count++) {
              while (retryCount < maxRetries) {
                long requestStartTime = System.currentTimeMillis();
                try {
                  ApiResponse<Void> reviewResult = likeApiInstance.reviewWithHttpInfo("like", id, ip);
                  long requestEndTime = System.currentTimeMillis();
                  long latencyPost = requestEndTime - requestStartTime;
                  if (reviewResult != null) {
                    recordLatency(recordMap, "LIKE", latencyPost, reviewResult.getStatusCode(), 2);
                    successfulRequests.updateAndGet(v -> v + 1);
                  } else {
                    failedRequests.updateAndGet(v -> v + 1);
                  }
                  totalRequests.updateAndGet(v -> v + 1);
                  requestSuccessful = true;
                  break;
                } catch (Exception e) {
                  failedRequests.updateAndGet(v -> v + 1);
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
              }

              //Dislike request
              retryCount = 0;
              maxRetries = 5;
              requestSuccessful = false;
              while (retryCount < maxRetries) {
                long requestStartTime = System.currentTimeMillis();
                try {
                  ApiResponse<Void> reviewResult = likeApiInstance.reviewWithHttpInfo("dislike", id, ip);
                  long requestEndTime = System.currentTimeMillis();
                  long latencyPost = requestEndTime - requestStartTime;
                  if (reviewResult != null) {
                    recordLatency(recordMap, "DISLIKE", latencyPost, reviewResult.getStatusCode(), 2);
                    successfulRequests.updateAndGet(v -> v + 1);
                  } else {
                    failedRequests.updateAndGet(v -> v + 1);
                  }
                  totalRequests.updateAndGet(v -> v + 1);
                  requestSuccessful = true;
                  break;
                } catch (Exception e) {
                  failedRequests.updateAndGet(v -> v + 1);
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
              }
            }
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
    writeLatencyRecordsToCSV(recordMap, "latency_records.csv");

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
    List<Long> latenciesReview = new ArrayList<>();
    List<LatencyRecord> postRecords = records.get(1);
    List<LatencyRecord> reviewRecords = records.get(2);
    for (LatencyRecord record : postRecords) {
      latenciesPost.add(record.getLatency());
    }
    for (LatencyRecord record : reviewRecords) {
      latenciesReview.add(record.getLatency());
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

    Collections.sort(latenciesReview);
    size = latenciesReview.size();
    min = latenciesReview.get(0);
    max = latenciesReview.get(size - 1);
    mean = latenciesReview.stream().mapToLong(Long::longValue).sum() / size;
    median = size % 2 == 0 ? (latenciesReview.get(size / 2 - 1) + latenciesReview.get(size / 2)) / 2 : latenciesReview.get(size / 2);
    p99Index = (int) Math.ceil(0.99 * size);
    p99 = latenciesReview.get(p99Index - 1);

    System.out.println("Review Request: ");
    System.out.println("Min Latency: " + min+ " ms");
    System.out.println("Max Latency: " + max + " ms");
    System.out.println("Mean Latency: " + mean + " ms");
    System.out.println("Median Latency: " + median + " ms");
    System.out.println("99th Percentile Latency (p99): " + p99 + " ms");
  }
}