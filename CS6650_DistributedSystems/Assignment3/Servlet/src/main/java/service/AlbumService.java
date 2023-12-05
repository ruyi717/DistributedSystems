package service;

import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.UUID;
import model.AlbumInfo;
import org.bson.Document;
import org.bson.types.ObjectId;

public class AlbumService {
  private MongoCollection<Document> collection;
  private static final String DATABASE = "albumsDB";
  private static final String COLLECTION = "albums";

  public AlbumService(MongoClient mongoClient) {
    MongoDatabase database = mongoClient.getDatabase(DATABASE);
    this.collection = database.getCollection(COLLECTION);
  }

  public String createAlbum(long imageSizeInBytes, String profileP) {
    String[] lines = profileP.split("\n");
    String artistLine = lines[1].trim();
    String titleLine = lines[2].trim();
    String yearLine = lines[3].trim();
    String artist = artistLine.split(":")[1].trim();
    String title = titleLine.split(":")[1].trim();
    String year = yearLine.split(":")[1].trim();
    String albumID = "1203";
    String fileName = UUID.randomUUID().toString() + ".jpg"; // Generate a unique file name


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
    return jsonResponse;
  }

  public String findAlbum(String id) throws Exception {
    // Query MongoDB for the album by albumID
    Document album = collection.find(new Document("_id", new ObjectId(id))).first();

    // You can return a constant JSON response for the given albumID
    if (album != null) {
      Document albumInf = album.get("albumInf", Document.class);
      AlbumInfo albumInfo = new AlbumInfo();
      albumInfo.setArtist(albumInf.getString("artist"));
      albumInfo.setTitle(albumInf.getString("title"));
      albumInfo.setYear(albumInf.getString("year"));

      // Convert the model.AlbumInfo to JSON and return it
      String jsonResponse = new Gson().toJson(albumInfo); // You can use any JSON library for this
      return jsonResponse;
    } else {
      throw new Exception("Album not found");
    }
  }
}
