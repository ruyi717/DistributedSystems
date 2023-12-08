package albumstore.entity;

public class ReviewBody {
  private String id;

  private Likes likes;

  public ReviewBody() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Likes getReviewProfile() {
    return likes;
  }

  public void setReviewProfile(Likes likes) {
    this.likes = likes;
  }
}
