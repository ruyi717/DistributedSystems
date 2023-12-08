import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.api.LikeApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;
import io.swagger.client.model.Likes;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class GetReviewThread implements Runnable {
    private static int RETRY_TIMES = 5;
    private int numRequests;
    private String ipAddress;
    private RequestCounter counter;
    private CountDownLatch countDownLatch;
    private RequestRecordWriter writer;
    private volatile boolean exit;

    public GetReviewThread(int numRequests, String ipAddress, RequestCounter counter, CountDownLatch countDownLatch, RequestRecordWriter writer) {
        this.numRequests = numRequests;
        this.ipAddress = ipAddress;
        this.counter = counter;
        this.countDownLatch = countDownLatch;
        this.writer = writer;
        this.exit = false;
    }

    @Override
    public void run() {
        LikeApi likeApiInstance = new LikeApi();
        likeApiInstance.getApiClient().setBasePath(ipAddress);
        while (!exit) {
            for (int i = 0; i < numRequests; i++) {
                boolean failedGetReviewRequest = false;
                for (int j = 0; j < RETRY_TIMES; j++) {
                    // Get review request
                    try {
                        long startTime = System.currentTimeMillis();
                        String randomAlbumID = writer.getValidID();
                        ApiResponse<Likes> response = likeApiInstance.getLikesWithHttpInfo(randomAlbumID);
                        if (counter != null) counter.incrementSuccessGetReviewRequest(1);
                        long endTime = System.currentTimeMillis();
                        if (writer != null) writer.addRequestRecord(
                                new RequestRecord(
                                        startTime,
                                        RequestRecord.RequestType.GET,
                                        endTime - startTime,
                                        response.getStatusCode())
                        );
                        failedGetReviewRequest = false;
                        break;
                    } catch (ApiException e) {
                        failedGetReviewRequest = true;
                        System.out.println("Exception when calling LikeApi:getReview");
                    }
                }

                if (failedGetReviewRequest && counter != null) counter.incrementFailGetReviewRequest(1);
            }
        }
        countDownLatch.countDown();
    }

    public void terminate() {
        this.exit = true;
    }
}
