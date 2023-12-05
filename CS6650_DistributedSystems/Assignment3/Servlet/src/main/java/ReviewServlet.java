import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.google.gson.Gson;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;


public class ReviewServlet extends HttpServlet {
  private final static String QUEUE_NAME = "album_review_queue";
  private ObjectPool<Channel> pool;

  @Override
  public void init() throws ServletException {
    this.pool = new GenericObjectPool<Channel>(new ConnectionPool());
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    String urlPath = request.getPathInfo();

    if (urlPath == null || urlPath.split("/").length != 3) {
      outputResponse(response, "Invalid URL path", HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    JsonObject jsonResponse = new JsonObject();
    Gson gson = new Gson();

    String[] urlParts = urlPath.split("/");
    boolean like = urlParts[1].equals("like");
    String id = urlParts[2];

    JsonObject reviewItem = new JsonObject();
    reviewItem.addProperty("albumID", id);
    reviewItem.addProperty("ifLike", like);
    Channel channel = null;

    try {
      channel = pool.borrowObject();
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);
      channel.basicPublish("", QUEUE_NAME, null, reviewItem.toString().getBytes());
    } catch (Exception e) {
      throw new RuntimeException("Unable to borrow from pool" + e.toString());
    } finally {
      try {
        if (channel != null) {
          System.out.println("Channel return Done");
          pool.returnObject(channel);
        }
      } catch (Exception e) {
        System.out.println("Error when returning channel");
      }
    }
//    this.outputResponse(response, jsonResponse, rc);
    jsonResponse.addProperty("message", "Review processed successfully");
    jsonResponse.addProperty("albumID", id);
    jsonResponse.addProperty("like", like);
    outputResponse(response, gson.toJson(jsonResponse), HttpServletResponse.SC_OK);
  }

  private void outputResponse(HttpServletResponse response, String payload, int status) {
    response.setHeader("Content-Type", "application/json");
    try {
      response.setStatus(status);
      if (payload != null) {
        OutputStream outputStream = response.getOutputStream();
        outputStream.write(payload.getBytes());
        outputStream.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
