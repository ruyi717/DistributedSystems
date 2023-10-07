public class Album {
  private String artist;
  private String title;
  private Integer year;

  public Album(String artist, String title, Integer year) {
    this.artist = artist;
    this.title = title;
    this.year = year;
  }

  public Album() {
  }

  public String getArtist() {
    return artist;
  }

  public void setArtist(String artist) {
    this.artist = artist;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }
}
