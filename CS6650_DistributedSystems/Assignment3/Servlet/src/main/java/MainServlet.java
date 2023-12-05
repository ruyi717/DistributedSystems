import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import service.AlbumService;
import service.ReviewService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@MultipartConfig
public class MainServlet extends HttpServlet {
  private MongoClient mongoClient;
  private static final String MONGODB_CONNECTIONSTRING = "mongodb://yourNewUser:yourNewPassword@34.210.24.129:27017";
  private AlbumService albumService;
  private ReviewService reviewService;
  private static final Logger logger = LogManager.getLogger(MainServlet.class);


  public MainServlet() {
    connect();
    this.albumService = new AlbumService(this.mongoClient);
    this.reviewService = new ReviewService(this.mongoClient);
  }


  public void connect(){
    ConnectionString connString = new ConnectionString(MONGODB_CONNECTIONSTRING);
    MongoClientSettings settings = MongoClientSettings.builder()
        .applyToClusterSettings(builder ->
            builder.hosts(
                Collections.singletonList(new ServerAddress(connString.getHosts().get(0))))
        )
        .credential(connString.getCredential())
        .build();
    mongoClient = MongoClients.create(settings);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    int rc = HttpServletResponse.SC_OK;
    String jsonResponse = "";
    Gson gson = new Gson();

    try {
      Part filePart = req.getPart("image");
      long imageSizeInBytes = filePart.getSize();
      String profileP = req.getParameter("profile");
      jsonResponse = this.albumService.createAlbum(imageSizeInBytes, profileP);
      Map<String, Object> map = new HashMap<>();
      try {
         map = gson.fromJson(jsonResponse, Map.class);
      } catch (JsonSyntaxException e) {
        logger.error("JSON Parsing Error: " + e.getMessage(), e);
        rc = HttpServletResponse.SC_BAD_REQUEST;
        jsonResponse = gson.toJson(new Error("JSON parsing error"));
      }
    } catch (Exception e) {
      rc = HttpServletResponse.SC_BAD_REQUEST;
      jsonResponse = gson.toJson(new Error("Exception in doPost: " + e.getMessage()));
    }
    this.outputResponse(resp, jsonResponse, rc);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    int rc = HttpServletResponse.SC_OK;
    String jsonResponse = "";
    Gson gson = new Gson();
    String objectID = "65432384e9a9a2280e0bf415"; // Extract albumID from the request URL

    try {
      jsonResponse = this.albumService.findAlbum(objectID);
    } catch (Exception e) {
      rc = HttpServletResponse.SC_BAD_REQUEST;
      jsonResponse = gson.toJson(new Error("invalid request"));
    }
    this.outputResponse(resp, jsonResponse, rc);
  }

  private void outputResponse(HttpServletResponse response, String jsonResponse, int status)
      throws IOException {
    response.setContentType("application/json");
    response.setStatus(status);
    response.getWriter().write(jsonResponse);
    response.setStatus(HttpServletResponse.SC_OK);
  }
}
