package io.swagger.client.api;

import io.swagger.client.*;
import io.swagger.client.model.*;

import java.io.File;
import java.util.List;

public class DefaultApiPost {
  public static ImageMetaData main(String uri, DefaultApi apiInstance) {
    File image = new File("/Users/jouy/desktop/DistributedSystems/CS6650_DistributedSystems/Assignment2/Assignment2_Client1/src/main/java/io/swagger/client/api/image.jpg");
    AlbumsProfile profile = new AlbumsProfile();// AlbumsProfile |
    profile.setArtist("Faye Wang");
    profile.setTitle("Dream");
    profile.setYear("1999");
    String postURI = uri;
    try {
      ImageMetaData result = apiInstance.newAlbum(image, profile, postURI);
      return result;
    } catch (ApiException e) {
      System.err.println("Exception when calling DefaultApi#newAlbum");
      e.printStackTrace();
    }
    return null;
  }
}