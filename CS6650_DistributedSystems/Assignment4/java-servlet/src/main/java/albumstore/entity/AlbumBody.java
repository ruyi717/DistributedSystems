package albumstore.entity;

public class AlbumBody {
    private String id;

    private byte[] image;

    private AlbumProfile profile;

    public AlbumBody() {}

    public AlbumBody(byte[] image, AlbumProfile profile) {
        this.image = image;
        this.profile = profile;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public AlbumProfile getProfile() {
        return profile;
    }

    public void setProfile(AlbumProfile profile) {
        this.profile = profile;
    }
}
