package albumstore.service;

import albumstore.entity.AlbumBody;
import albumstore.entity.AlbumProfile;
import albumstore.entity.Likes;
import albumstore.entity.ReviewBody;
import org.bson.Document;
import org.bson.types.ObjectId;

public class ObjectDocumentConverter {
    public static Document albumProfileToDocument(AlbumProfile albumProfile) {
        Document doc = new Document();
        doc.append("artist", albumProfile.getArtist());
        doc.append("title", albumProfile.getTitle());
        doc.append("year", albumProfile.getYear());
        return doc;
    }

    public static AlbumProfile toAlbumProfile (Document doc) {
        AlbumProfile albumProfile = new AlbumProfile();
        albumProfile.setArtist((String) doc.get("artist"));
        albumProfile.setTitle((String) doc.get("title"));
        albumProfile.setYear((String) doc.get("year"));
        return albumProfile;
    }

    public static Likes toLikes (Document doc) {
        Likes likes = new Likes();
        likes.setLikes((Integer) doc.get("likes"));
        likes.setDislikes((Integer) doc.get("dislikes"));
        return likes;
    }

    public static Document albumBodyToDocument(AlbumBody albumBody) {
        Document doc = new Document();
        doc.append("image", albumBody.getImage());
        doc.append("profile", albumProfileToDocument(albumBody.getProfile()));
        if (albumBody.getId() != null) {
            doc.append("_id", new ObjectId(albumBody.getId()));
        }
        return doc;
    }

    public static Document reviewToDocument(String id) {
        Document doc = new Document();
        doc.append("_id", new ObjectId(id));
        doc.append("likes", 0);
        doc.append("dislikes", 0);
        return doc;
    }

    public static AlbumBody toAlbumBody (Document doc) {
        AlbumBody albumBody = new AlbumBody();
        albumBody.setImage(doc.get("image", org.bson.types.Binary.class).getData());
        albumBody.setProfile(toAlbumProfile((Document) doc.get("profile")));
        albumBody.setId(((ObjectId) doc.get("_id")).toString());
        return albumBody;
    }

    public static int toSequence(Document doc) {
        return (int) doc.get("seq");
    }
}
