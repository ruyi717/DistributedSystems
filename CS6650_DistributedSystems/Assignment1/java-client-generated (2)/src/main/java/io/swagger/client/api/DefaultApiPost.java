package io.swagger.client.api;

import io.swagger.client.*;
import io.swagger.client.model.*;

import java.io.File;

public class DefaultApiPost {

  public static void main(String[] args) {

    DefaultApi apiInstance = new DefaultApi();
    File image = new File("/Users/jouy/desktop/DistributedSystems/CS6650_DistributedSystems/Assignment1/java-client-generated (2)/src/main/java/io/swagger/client/api/image.jpg");
    AlbumsProfile profile = new AlbumsProfile();// AlbumsProfile |
    profile.setArtist("Taylor Swift");
    profile.setTitle("Evermore");
    profile.setYear("2020");
    String postURI = args[0];
    try {
      ImageMetaData result = apiInstance.newAlbum(image, profile, postURI);
//      System.out.println(result);
    } catch (ApiException e) {
//      System.err.println("Exception when calling DefaultApi#newAlbum");
      e.printStackTrace();
    }
  }
}