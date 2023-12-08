import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.api.LikeApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class ClientThread implements Runnable {
    private static int RETRY_TIMES = 5;
    private int numRequests;
    private String ipAddress;
    private RequestCounter counter;
    private CountDownLatch countDownLatch;
    private String albumID;
    private File image;
    private AlbumsProfile albumsProfile;
    private RequestRecordWriter writer;

    public ClientThread(int numRequests, String ipAddress, RequestCounter counter, CountDownLatch countDownLatch, String albumID, File image, AlbumsProfile albumsProfile, RequestRecordWriter writer) {
        this.numRequests = numRequests;
        this.ipAddress = ipAddress;
        this.counter = counter;
        this.countDownLatch = countDownLatch;
        this.albumID = albumID;
        this.albumsProfile = albumsProfile;
        this.image = image;
        this.writer = writer;
    }

    public ClientThread(int numRequests, String ipAddress, CountDownLatch countDownLatch, String albumID, File image, AlbumsProfile albumsProfile) {
        this.numRequests = numRequests;
        this.ipAddress = ipAddress;
        this.countDownLatch = countDownLatch;
        this.albumID = albumID;
        this.albumsProfile = albumsProfile;
        this.image = image;
    }

    @Override
    public void run() {
        DefaultApi apiInstance = new DefaultApi();
        LikeApi likeApiInstance = new LikeApi();
        apiInstance.getApiClient().setBasePath(ipAddress);
        likeApiInstance.getApiClient().setBasePath(ipAddress);
        File image = new File("src/nmtb.png");
        for (int i = 0; i < numRequests; i++) {
            boolean failedNewAlbumRequest = false;
            boolean failedReviewAlbumRequest = false;
            String id = "";
            for (int j = 0; j < RETRY_TIMES; j++) {
                // POST new album request
                try {
                    long startTime = System.currentTimeMillis();
                    ApiResponse<ImageMetaData> response = apiInstance.newAlbumWithHttpInfo(image, albumsProfile);
                    if (counter != null) counter.incrementSuccessRequest(1);
                    long endTime =System.currentTimeMillis();
                    if (writer != null) writer.addRequestRecord(
                            new RequestRecord(
                                    startTime,
                                    RequestRecord.RequestType.POST,
                                    endTime - startTime,
                                    response.getStatusCode())
                    );
                    failedNewAlbumRequest = false;
                    id = response.getData().getAlbumID();
                    if (writer != null) writer.addValidIDs(id);
                    break;
                } catch (ApiException e) {
                    failedNewAlbumRequest = true;
                    System.out.println("Exception when calling DefaultApi:newAlbum");
                    System.out.println(e);
                }
            }
            if (!id.equals("")) {
                for (int k = 0; k < 2; k++) {
                    for (int j = 0; j < RETRY_TIMES; j++) {
                        // POST review album request
                        try {
                            long startTime = System.currentTimeMillis();
                            ApiResponse<Void> response = likeApiInstance.reviewWithHttpInfo("like", id);
                            if (counter != null) counter.incrementSuccessRequest(1);
                            long endTime =System.currentTimeMillis();
                            if (writer != null) writer.addRequestRecord(
                                    new RequestRecord(
                                            startTime,
                                            RequestRecord.RequestType.POST,
                                            endTime - startTime,
                                            response.getStatusCode())
                            );
                            failedReviewAlbumRequest = false;
                            break;
                        } catch (ApiException e) {
                            failedReviewAlbumRequest = true;
                            System.out.println("Exception when calling LikeApi:review");
                        }
                    }
                }
                for (int j = 0; j < RETRY_TIMES; j++) {
                    // POST review album request
                    try {
                        long startTime = System.currentTimeMillis();
                        ApiResponse<Void> response = likeApiInstance.reviewWithHttpInfo("dislike", id);
                        if (counter != null) counter.incrementSuccessRequest(1);
                        long endTime =System.currentTimeMillis();
                        if (writer != null) writer.addRequestRecord(
                                new RequestRecord(
                                        startTime,
                                        RequestRecord.RequestType.POST,
                                        endTime - startTime,
                                        response.getStatusCode())
                        );
                        failedReviewAlbumRequest = false;
                        break;
                    } catch (ApiException e) {
                        failedReviewAlbumRequest = true;
                        System.out.println("Exception when calling LikeApi:review");
                    }
                }
            }

            if (failedReviewAlbumRequest && counter != null) counter.incrementFailRequest(1);
            if (failedNewAlbumRequest && counter != null) counter.incrementFailRequest(1);
        }
        countDownLatch.countDown();
    }
}
