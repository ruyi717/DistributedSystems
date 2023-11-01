import com.google.gson.Gson;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bson.Document;


@WebServlet("/albums/*")
public class AlbumInfoServlet extends HttpServlet {
  private MongoClient mongoClient;
  @Override
  public void init() throws ServletException {
    System.out.println("reach init get");
    String connectionString = "mongodb://yourNewUser:yourNewPassword@35.92.47.29:27017/albumsDB";
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
    System.out.println("reach get");
    String albumID = request.getPathInfo().substring(1); // Extract albumID from the request URL
    System.out.println("Received albumID: " + albumID);
    MongoDatabase database = mongoClient.getDatabase("albumsDB"); // Use the correct database name
    MongoCollection<Document> collection = database.getCollection("albums");

    // Query MongoDB for the album by albumID
    Document album = collection.find(new Document("albumId", albumID)).first();
    System.out.println("album is "+ album.toJson());

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
//      String jsonResponse = album.toJson();
//      response.setContentType("application/json");
//      response.getWriter().write(jsonResponse);
//      response.setStatus(HttpServletResponse.SC_OK);
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
