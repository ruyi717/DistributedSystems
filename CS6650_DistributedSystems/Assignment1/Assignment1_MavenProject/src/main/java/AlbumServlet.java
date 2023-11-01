import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    System.out.println("reach init post]");
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
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    System.out.println("reach post");
    MongoDatabase database = mongoClient.getDatabase("albumsDB");
    MongoCollection<Document> collection = database.getCollection("albums");

    try {
//       Handle file upload
      Part filePart = request.getPart("image");
      String fileName = UUID.randomUUID().toString() + ".jpg"; // Generate a unique file name
      byte[] inputStream = filePart.getInputStream().readAllBytes();
      long imageSizeInBytes = (long) inputStream.length;
      // Specify the directory where you want to save the uploaded files
//      OutputStream outputStream = new FileOutputStream(
//          "/Users/jouy/Desktop/DistributedSystems/CS6650_DistributedSystems/Assignment2/"
//              + fileName);

//      byte[] buffer = new byte[1024];
//      int bytesRead;
//      long imageSizeInBytes = 0; // Initialize the size counter
//
//      while ((bytesRead = inputStream.read(buffer)) != -1) {
//        outputStream.write(buffer, 0, bytesRead);
//        imageSizeInBytes += bytesRead; // Update the image size
//      }
//      outputStream.close();
//      inputStream.close();

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