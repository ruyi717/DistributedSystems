import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.UUID;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

@WebServlet("/albums")
@MultipartConfig
public class AlbumServlet extends HttpServlet {

  private MongoClient mongoClient;

  @Override
  public void init() throws ServletException {
    String connectionString = "mongodb://yourNewUser:yourNewPassword@34.221.179.233:27017";
    ConnectionString connString = new ConnectionString(connectionString);
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
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    MongoDatabase database = mongoClient.getDatabase("albumsDB");
    MongoCollection<Document> collection = database.getCollection("albums");

    try {
//       Handle file upload
      Part filePart = request.getPart("image");
      String fileName = UUID.randomUUID().toString() + ".jpg"; // Generate a unique file name

      long imageSizeInBytes = filePart.getSize();
      // Generate a new album key (albumID)
      String profileP = request.getParameter("profile");

      String[] lines = profileP.split("\n");
      String artistLine = lines[1].trim();
      String titleLine = lines[2].trim();
      String yearLine = lines[3].trim();

      String artist = artistLine.split(":")[1].trim();
      String title = titleLine.split(":")[1].trim();
      String year = yearLine.split(":")[1].trim();
      String albumID = "10";

      // Create the album document for MongoDB
      Document albumDocument = new Document("image", fileName)
          .append("albumId", albumID)
          .append("albumInf", new Document("artist", artist)
              .append("title", title)
              .append("year", year));

      // Insert the album document into MongoDB
      collection.insertOne(albumDocument);

      // Create the JSON response
      String imageSize = String.valueOf(imageSizeInBytes);

      String jsonResponse = String.format("{\"albumID\": \"%s\", \"imageSize\": \"%s\"}", albumID,
          imageSize);

      // Return a JSON response
      response.setContentType("application/json");
      response.getWriter().write(jsonResponse);
      response.setStatus(HttpServletResponse.SC_OK);
    } catch (Exception e) {
      System.out.println("exception: " + e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public void destroy() {
    // Close the MongoDB client when the servlet is being destroyed (e.g., when the server is shut down).
    if (mongoClient != null) {
      mongoClient.close();
    }
  }
}