public class ReviewItem {
    private String albumID;
    private boolean ifLike;

    public ReviewItem() {}

    public ReviewItem(String id, boolean ifLike) {
        this.albumID = id;
        this.ifLike = ifLike;
    }

    public String getId() {
        return albumID;
    }

    public boolean isIfLike() {
        return ifLike;
    }

    public void setIfLike(boolean ifLike) {
        this.ifLike = ifLike;
    }
}
