import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerLoadTester {

  public static void main(String[] args) {
    if (args.length != 4) {
      System.out.println("Usage: java ServerLoadTester <threadGroupSize> <numThreadGroups> <delay> <IPAddr>");
      System.exit(1);
    }

    int threadGroupSize = Integer.parseInt(args[0]);
    int numThreadGroups = Integer.parseInt(args[1]);
    int delay = Integer.parseInt(args[2]);
    String serverURI = args[3];

    long startTime = System.currentTimeMillis();
    int totalRequests = 0;
    int successfulRequests = 0;

    // Create an ExecutorService for managing threads
    ExecutorService executor = Executors.newFixedThreadPool(threadGroupSize);

    // Loop through the thread groups
    for (int i = 0; i < numThreadGroups; i++) {
      System.out.println("Starting Thread Group " + (i + 1));

      // Create and start threads in the current thread group
      for (int j = 0; j < threadGroupSize; j++) {
        executor.submit(new LoadTestRunnable(serverURI, successfulRequests));
      }

      // Sleep for the specified delay before starting the next thread group
      try {
        Thread.sleep(delay * 1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    // Shutdown the executor and wait for all threads to finish
    executor.shutdown();
    try {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // Calculate the Wall Time
    long endTime = System.currentTimeMillis();
    long wallTime = (endTime - startTime) / 1000;

    // Calculate Throughput
    double throughput = (double) successfulRequests / wallTime;

    // Output results
    System.out.println("Wall Time: " + wallTime + " seconds");
    System.out.println("Throughput: " + throughput + " requests/second");
  }

  static class LoadTestRunnable implements Runnable {
    private final String serverURI;
    private int successfulRequests;

    public LoadTestRunnable(String serverURI, int successfulRequests) {
      this.serverURI = serverURI;
      this.successfulRequests = successfulRequests;
    }

    @Override
    public void run() {
      for (int k = 0; k < 100; k++) {
        // Retry up to 5 times for 4XX and 5XX response codes
        for (int retryCount = 0; retryCount < 5; retryCount++) {
          try {
            // Create an HTTP client
            HttpClient client = HttpClient.newHttpClient();

            // Create a POST request
            HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(serverURI))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

            // Send the POST request
            HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

            if (postResponse.statusCode() == 200) {
              successfulRequests++;
              break; // Success, exit retry loop
            } else if (postResponse.statusCode() >= 400 && postResponse.statusCode() < 600) {
              // Retry for 4XX and 5XX response codes
              continue; // Retry the request
            }

            // Create a GET request
            HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(serverURI))
                .GET()
                .build();

            // Send the GET request
            HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

            if (getResponse.statusCode() == 200) {
              successfulRequests++;
              break; // Success, exit retry loop
            } else if (getResponse.statusCode() >= 400 && getResponse.statusCode() < 600) {
              // Retry for 4XX and 5XX response codes
              continue; // Retry the request
            }

          } catch (Exception e) {
            // Handle exceptions here
            e.printStackTrace();
          }
        }
      }
    }
  }
}
