package io.swagger.client.api;

import io.swagger.client.*;
import io.swagger.client.model.*;

public class DefaultApiGet {

  public static void main(String[] args) {

    DefaultApi apiInstance = new DefaultApi();
    String uri = args[0];
    String albumID = "1"; // String | path  parameter is album key to retrieve
    try {
      AlbumInfo result = apiInstance.getAlbumByKey(albumID, uri);
//      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DefaultApi#getAlbumByKey");
      e.printStackTrace();
    }
  }
}
