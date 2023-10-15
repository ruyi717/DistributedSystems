import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

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
    String serverURI = args[3];
    String getURI = serverURI + "/1";

    AtomicReference<Integer> totalRequests = new AtomicReference<>(0);
    RequestCounterBarrier counter = new RequestCounterBarrier();
    CountDownLatch completed = new CountDownLatch(10);
    HttpClient client = new HttpClient();

    for (int i = 0; i < 10; i++) {
      Runnable thread = () -> {
        counter.inc();
        completed.countDown();
      };
      new Thread(thread).start();
      HttpMethod postMethod = new PostMethod(serverURI);
      HttpMethod getMethod = new GetMethod(getURI);
      doMethods(client, postMethod);
      doMethods(client, getMethod);
    }
    completed.await();

    long startTime = System.currentTimeMillis();

    for (int threadGroup = 0; threadGroup < numThreadGroups; threadGroup++) {
      for (int i = 0; i < threadGroupSize; i++) {
        for (int j = 0; j < 1000; j++) {
          Runnable thread = () -> {
            counter.inc();
            completed.countDown();
          };
          new Thread(thread).start();
          HttpMethod postMethod = new PostMethod(serverURI);
          HttpMethod getMethod = new GetMethod(getURI);
          doMethods(client, postMethod);
          doMethods(client, getMethod);
          totalRequests.updateAndGet(v -> v + 2);
        }
      }
      try {
        Thread.sleep(delay * 1000); // Sleep for 'delay' seconds between thread groups
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    long endTime = System.currentTimeMillis();
    long wallTime = (endTime - startTime) / 1000;
    double throughput = (double) totalRequests.get() / wallTime;

    System.out.println("Wall Time: " + wallTime + " seconds");
    System.out.println("Throughput: " + throughput + " requests/second");
  }

  private static void doMethods(HttpClient client, HttpMethod method) throws InterruptedException {
    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
        new DefaultHttpMethodRetryHandler(3, false));

    int retryCount = 0;
    int maxRetries = 5;
    boolean requestSuccessful = false;
    long startTime = System.currentTimeMillis();

    while (retryCount < maxRetries) {
      try {
        // Execute the method.
        int statusCode = client.executeMethod(method);
        if (statusCode != HttpStatus.SC_OK) {
          System.err.println("Method failed: " + method.getStatusLine());
        } else {
          requestSuccessful = true;
          break; // Request was successful, exit the loop
        }

        // Read the response body.
        byte[] responseBody = method.getResponseBody();

        // Deal with the response.
        // Use caution: ensure correct character encoding and is not binary data
        System.out.println("GET API: " + new String(responseBody));

      } catch (HttpException e) {
        System.err.println("Fatal protocol violation: " + e.getMessage());
        e.printStackTrace();
      } catch (IOException e) {
        System.err.println("Fatal transport error: " + e.getMessage());
        e.printStackTrace();
      } finally {
        // Release the connection.
        method.releaseConnection();
      }
      retryCount++;
      if (retryCount < maxRetries) {
        Thread.sleep(1000);
      }
    }

    if (!requestSuccessful) {
      System.err.println("Request failed after " + maxRetries + " retries: " + method.getPath());
    }
  }
}