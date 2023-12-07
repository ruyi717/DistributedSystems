package albumstore.service;

import albumstore.entity.AlbumBody;
import albumstore.entity.AlbumProfile;
import albumstore.entity.ImageMetaData;
import albumstore.exception.BadRequestException;
import albumstore.exception.NotFoundException;
import com.google.gson.Gson;
//import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

public class AlbumBodyService {
    private MongoCollection<Document> collection;
    private static final String DB_NAME = "albumStore";
    private static final String DB_COLLECTION_NAME = "albums";

    public AlbumBodyService(MongoClient mongo) {
        this.collection = mongo.getDatabase(DB_NAME).getCollection(DB_COLLECTION_NAME);
    }

    /**
     * Find an album by id
     * @param id
     * @return
     * @throws NotFoundException
     */
    public String findAlbumByID(String id) throws NotFoundException {
        Document doc = this.collection.find(Filters.eq("_id", new ObjectId(id))).first();
        if (doc != null) {
            return this.toJson(ObjectDocumentConverter.toAlbumBody(doc).getProfile());
        } else {
            throw new NotFoundException("Album not found");
        }
    }

    /**
     * Create album from json payload
     * @param jsonPayload
     * @return
     */
    public String createAlbum(byte[] image, String jsonPayload) throws BadRequestException {
        if (jsonPayload == null) return null;

        Gson gson = new Gson();
        AlbumProfile newAlbumProfile = gson.fromJson(jsonPayload, AlbumProfile.class);
        AlbumBody newAlbumBody = new AlbumBody(image, newAlbumProfile);

        if (newAlbumBody != null) {
            Document doc = ObjectDocumentConverter.albumBodyToDocument(newAlbumBody);
            this.collection.insertOne(doc);
            ObjectId id = (ObjectId) doc.get("_id");
            newAlbumBody.setId(id.toString());
            ImageMetaData imageMetaData =
                    new ImageMetaData(
                            id.toString(),
                            String.valueOf(image.length));
            return this.toJson(imageMetaData);
        } else {
            throw new BadRequestException("newAlbumBody == null. invalid request");
        }
    }

    private String toJson(Object list) {
        if (list == null) return null;
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}
