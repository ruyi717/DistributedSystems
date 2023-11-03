import com.google.gson.Gson;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.bson.types.ObjectId;


@WebServlet("/albums/*")
public class AlbumInfoServlet extends HttpServlet {
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
  protected void doGet(
      HttpServletRequest request, HttpServletResponse response) throws IOException {
    String objectID = "6543233ef0009eeeebfb1dd4"; // Extract albumID from the request URL
    MongoDatabase database = mongoClient.getDatabase("albumsDB"); // Use the correct database name
    MongoCollection<Document> collection = database.getCollection("albums");

    // Query MongoDB for the album by albumID
    Document album = collection.find(new Document("_id", new ObjectId(objectID))).first();

    // You can return a constant JSON response for the given albumID
    if (album != null) {
      Document albumInf = album.get("albumInf", Document.class);
      AlbumInfo albumInfo = new AlbumInfo();
      albumInfo.setArtist(albumInf.getString("artist"));
      albumInfo.setTitle(albumInf.getString("title"));
      albumInfo.setYear(albumInf.getString("year"));

      // Convert the AlbumInfo to JSON and return it
      String jsonResponse = new Gson().toJson(albumInfo); // You can use any JSON library for this
      response.setContentType("application/json");
      response.getWriter().write(jsonResponse);
      response.setStatus(HttpServletResponse.SC_OK);
    } else {
      // Handle the case when the requested albumID is not found
      String errorMessage = "{\"msg\": \"Album not found\"}";
      response.setContentType("application/json");
      response.getWriter().write(errorMessage);
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
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
