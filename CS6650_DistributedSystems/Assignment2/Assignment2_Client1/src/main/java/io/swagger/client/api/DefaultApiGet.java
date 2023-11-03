package io.swagger.client.api;

import io.swagger.client.*;
import io.swagger.client.model.*;
import java.util.List;

public class DefaultApiGet {

  public static AlbumInfo main(String uri,DefaultApi apiInstance ) {
    apiInstance = new DefaultApi();
    String albumID = "1"; // String | path  parameter is album key to retrieve
    try {
      AlbumInfo result = apiInstance.getAlbumByKey(albumID, uri);
      return result;
    } catch (ApiException e) {
      System.err.println("Exception when calling DefaultApi#getAlbumByKey");
      e.printStackTrace();
    }
    return null;
  }
}
