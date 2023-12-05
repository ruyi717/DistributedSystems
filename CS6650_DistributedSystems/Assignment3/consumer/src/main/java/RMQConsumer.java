import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RMQConsumer {
  private static final String QUEUE_NAME = "album_review_queue";
  private static final String RABBITMQ_HOST = "54.186.190.248";
  private static final String DB_ADDRESS = "34.210.24.129";
  private static final int NUM_THREADS = 32;
  private static final int DB_PORT = 27017;


  public static void main(String[] args) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(RABBITMQ_HOST);
    factory.setPort(5672);
    factory.setUsername("ruyi");
    factory.setPassword("password");
    Connection connection = factory.newConnection();

    Runnable runnable = () -> {
      try {
        MongoClient mongoClient = new MongoClient(DB_ADDRESS, DB_PORT);
        final Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.basicQos(30);
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");
          writeToDB(mongoClient, message);
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
      } catch (IOException e) {
        Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, e);
      }
    };

    for (int i = 0; i < NUM_THREADS; i++) {
      Thread thread = new Thread(runnable);
      thread.start();
    }
  }

  public static void writeToDB(MongoClient mongoClient, String message) {
    try{
      Gson gson = new Gson();
      String DB_NAME = "albumsDB";
      String DB_COLLECTION_NAME = "reviews";
      MongoCollection<Document> collection = mongoClient.getDatabase(DB_NAME).getCollection(DB_COLLECTION_NAME);
      ReviewItem reviewItem = gson.fromJson(message, ReviewItem.class);
      String customId = reviewItem.getId();
      Document filter = new Document("customId", customId);
      String fieldName = reviewItem.isIfLike() ? "like" : "dislike";
      Document update = new Document("$inc", new Document(fieldName, 1));
      UpdateResult updateResult = collection.updateOne(filter, update);
      if (updateResult.wasAcknowledged() && updateResult.getModifiedCount() > 0) {
        System.out.println("Message written to MongoDB");
      }
    } catch (Exception e) {
      System.out.println("writeToDB error: " + e);
    }
  }

}