import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RabbitMQConsumer {
    private static final String QUEUE_NAME = "album_review_queue";
    private static final String RABBITMQ_HOST = "172.31.16.177"; // RabbitMQ server host
    private static final String DB_ADDRESS = "172.31.27.253";
    private static final int DB_PORT = 27017;
    private static final int NUM_THREADS = 32;

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_HOST);
        factory.setUsername("katexuuu");
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
//                    System.out.println(message);
                    writeToDB(mongoClient, message);
//                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                };

                channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
            } catch (IOException e) {
                Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, e);
            }
        };

        // Begin to start our threads.
        for (int i = 0; i < NUM_THREADS; i++) {
            Thread thread = new Thread(runnable);
            thread.start();
            System.out.println("start  " + i);
        }
//        ExecutorService epool = Executors.newFixedThreadPool(NUM_THREADS);
//        for (int i = 0; i < NUM_THREADS; i++) {
//            epool.execute(runnable);
//        }
//        epool.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
    }

    public static void writeToDB(MongoClient mongoClient, String message) {
        Gson gson = new Gson();
        String DB_NAME = "albumStore";
        String DB_COLLECTION_NAME = "reviews";
        MongoCollection<Document> collection = mongoClient.getDatabase(DB_NAME).getCollection(DB_COLLECTION_NAME);

        ReviewItem reviewItem = gson.fromJson(message, ReviewItem.class);
        ObjectId documentId = new ObjectId(reviewItem.getId()); // Replace with the actual ObjectId
        Document filter = new Document("_id", documentId);
        String fieldName = reviewItem.isIfLike() ? "like" : "dislike";
        Document update = new Document("$inc", new Document(fieldName, 1));
        UpdateResult updateResult = collection.updateOne(filter, update);
        if (updateResult.wasAcknowledged() && updateResult.getModifiedCount() > 0) {
            System.out.println("Message written to MongoDB");
        }
    }

}
