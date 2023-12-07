package albumstore.service;

import albumstore.entity.AlbumBody;
import albumstore.entity.AlbumProfile;
import albumstore.entity.ReviewBody;
import albumstore.entity.ReviewProfile;
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

    public static ReviewProfile toReviewProfile (Document doc) {
        ReviewProfile reviewProfile = new ReviewProfile();
        reviewProfile.setLikes((Integer) doc.get("likes"));
        reviewProfile.setDislikes((Integer) doc.get("dislikes"));
        return reviewProfile;
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
        doc.append("id", id);
        doc.append("like", 0);
        doc.append("dislike", 0);
        return doc;
    }

    public static AlbumBody toAlbumBody (Document doc) {
        AlbumBody albumBody = new AlbumBody();
        albumBody.setImage(doc.get("image", org.bson.types.Binary.class).getData());
        albumBody.setProfile(toAlbumProfile((Document) doc.get("profile")));
        albumBody.setId(((ObjectId) doc.get("_id")).toString());
        return albumBody;
    }

    public static ReviewBody toReviewBody (Document doc) {
        ReviewBody reviewBody = new ReviewBody();
        reviewBody.setReviewProfile(toReviewProfile((Document) doc.get("reviewProfile")));
        reviewBody.setId((doc.get("id")).toString());
        return reviewBody;
    }

    public static int toSequence(Document doc) {
        return (int) doc.get("seq");
    }
}
