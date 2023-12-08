package albumstore.service;

import albumstore.exception.BadRequestException;
import albumstore.exception.NotFoundException;
import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.bson.types.ObjectId;

public class ReviewService {
    private MongoCollection<Document> collection;
    private static final String DB_NAME = "albumStore";
    private static final String DB_COLLECTION_NAME = "reviews";
    private static final Gson gson = new Gson();

    public ReviewService(MongoClient mongo) {
        this.collection = mongo.getDatabase(DB_NAME).getCollection(DB_COLLECTION_NAME);
        this.collection.createIndex(Indexes.ascending("_id"));
    }
    public String findReviewByID(String id) throws NotFoundException {
        Document doc = this.collection.find(Filters.eq("_id", new ObjectId(id))).first();
        if (doc != null) {
            return this.toJson(ObjectDocumentConverter.toLikes(doc));
        } else {
            throw new NotFoundException("Album not found");
        }
    }

//    public String addLike(String id) throws Exception {
//        // Check if the review exists
//        Document existingReview = collection.find(Filters.eq("id", id)).first();
//
//        if (existingReview != null) {
//            // Review exists, increment the Dislike count
//            try {
//                collection.updateOne(Filters.eq("id", id), Updates.inc("reviewProfile.likes", 1));
//                return this.collection.find(Filters.eq("id", id)).first().toJson();
//            } catch (Exception e) {
//                e.printStackTrace();
//                throw new Exception("invalid request");
//            }
//        } else {
//            // Review does not exist, create a new one with 1 dislike
//            Likes newReviewProfile = new Likes(1, 0);
//            try {
//                Document newReview = new Document("id", id)
//                    .append("reviewProfile", new Document()
//                        .append("likes", newReviewProfile.getLikes())
//                        .append("dislikes", newReviewProfile.getDislikes()));
//                collection.insertOne(newReview);
//                return this.collection.find(Filters.eq("id", id)).first().toJson();
//            } catch (Exception e) {
//                e.printStackTrace();
//                throw new Exception("invalid request");
//            }
//        }
//    }
//
//    public String addDislike(String id) throws Exception {
//        // Check if the review exists
//        Document existingReview = collection.find(Filters.eq("id", id)).first();
//
//        if (existingReview != null) {
//            // Review exists, increment the Dislike count
//            try {
//                collection.updateOne(Filters.eq("id", id), Updates.inc("reviewProfile.dislikes", 1));
//                return this.collection.find(Filters.eq("id", id)).first().toJson();
//            } catch (Exception e) {
//                e.printStackTrace();
//                throw new Exception("invalid request");
//            }
//        } else {
//            // Review does not exist, create a new one with 1 dislike
//            Likes newReviewProfile = new Likes(0, 1);
//            try {
//                Document newReview = new Document("id", id)
//                    .append("reviewProfile", new Document()
//                        .append("likes", newReviewProfile.getLikes())
//                        .append("dislikes", newReviewProfile.getDislikes()));
//                collection.insertOne(newReview);
//                return this.collection.find(Filters.eq("id", id)).first().toJson();
//            } catch (Exception e) {
//                e.printStackTrace();
//                throw new Exception("invalid request");
//            }
//        }
//    }

    /**
     * Create album in review collection
     * @param id
     */
    public void createReview(String id) throws BadRequestException {
        Document doc = ObjectDocumentConverter.reviewToDocument(id);
        this.collection.insertOne(doc);
    }

    private String toJson(Object list) {
        if (list == null) return null;
        String json = gson.toJson(list);
        return json;
    }
}

