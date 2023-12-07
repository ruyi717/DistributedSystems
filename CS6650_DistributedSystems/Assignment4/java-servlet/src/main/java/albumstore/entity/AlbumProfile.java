package albumstore.entity;

import com.google.gson.annotations.SerializedName;

public class AlbumProfile {

    @SerializedName(value = "artist", alternate = {"Artist"})
    private String artist;

    @SerializedName(value = "title", alternate = {"Title"})
    private String title;

    @SerializedName(value = "year", alternate = {"Year"})
    private String year;

    public AlbumProfile() {}

    public AlbumProfile(String artist, String title, String year) {
        this.artist = artist;
        this.title = title;
        this.year = year;
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

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "AlbumProfile{" +
                ", artist='" + artist + '\'' +
                ", title='" + title + '\'' +
                ", year='" + year + '\'' +
                '}';
    }
}
