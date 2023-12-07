package albumstore.entity;

public class ReviewBody {
  private String id;
  private ReviewProfile reviewProfile;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ReviewProfile getReviewProfile() {
    return reviewProfile;
  }

  public void setReviewProfile(ReviewProfile reviewProfile) {
    this.reviewProfile = reviewProfile;
  }
}
