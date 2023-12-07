package albumstore.entity;

import com.google.gson.annotations.SerializedName;

public class ReviewProfile {
  @SerializedName(value = "likes", alternate = {"Likes"})
  private Integer likes;

  @SerializedName(value = "dislikes", alternate = {"Dislikes"})
  private Integer dislikes;



  public ReviewProfile() {}

  public ReviewProfile(Integer likes, Integer dislikes) {
    this.likes = likes;
    this.dislikes = dislikes;
  }

  public Integer getLikes() {
    return likes;
  }

  public void setLikes(Integer likes) {
    this.likes = likes;
  }

  public Integer getDislikes() {
    return dislikes;
  }

  public void setDislikes(Integer dislikes) {
    this.dislikes = dislikes;
  }

  @Override
  public String toString() {
    return "ReviewProfile{" +
        "likes=" + likes +
        ", dislikes=" + dislikes +
        '}';
  }
}
