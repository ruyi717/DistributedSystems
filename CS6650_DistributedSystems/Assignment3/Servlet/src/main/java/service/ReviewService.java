package service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class ReviewService {
  private MongoCollection<Document> collection;
  private static final String DATABASE = "albumsDB";
  private static final String COLLECTION = "reviews";

  public ReviewService(MongoClient mongoClient) {
    MongoDatabase database = mongoClient.getDatabase(DATABASE);
    this.collection = database.getCollection(COLLECTION);
  }

  /**
   * Create album in review collection
   * @param id
   */
  public String createReview(String id) {
    Document doc = new Document();
    doc.append("albumId", id);
    doc.append("like", 0);
    doc.append("dislike", 0);
    collection.insertOne(doc);
    String jsonResponse = String.format("{\"albumID\": \"%s\", \"like\": \"%s\"}", id,
        "Like or Dislike");
    return jsonResponse;
  }

}
