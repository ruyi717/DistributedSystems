package albumstore.entity;

public class ImageMetaData {
    private String albumID = null;

    private String imageSize = null;

    public ImageMetaData(String albumID, String imageSize) {
        this.albumID = albumID;
        this.imageSize = imageSize;
    }

    public String getAlbumID() {
        return albumID;
    }

    public void setAlbumID(String albumID) {
        this.albumID = albumID;
    }

    public String getImageSize() {
        return imageSize;
    }

    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }

    @Override
    public String toString() {
        return "ImageMetaData{" +
                "albumID='" + albumID + '\'' +
                ", imageSize='" + imageSize + '\'' +
                '}';
    }
}
