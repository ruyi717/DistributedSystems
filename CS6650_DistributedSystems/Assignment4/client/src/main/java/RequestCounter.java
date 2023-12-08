public class RequestCounter {
    private int successRequest;
    private int failRequest;
    private int successGetReviewRequest;
    private int failGetReviewRequest;

    public RequestCounter() {
        this.successRequest = 0;
        this.failRequest = 0;
        this.successGetReviewRequest = 0;
        this.failGetReviewRequest = 0;
    }

    public synchronized void incrementSuccessRequest(int increment) {
        this.successRequest += increment;
    }

    public synchronized void incrementFailRequest(int increment) {
        this.failRequest += increment;
    }

    public synchronized void incrementSuccessGetReviewRequest(int increment) { this.successGetReviewRequest += increment; }

    public synchronized void incrementFailGetReviewRequest(int increment) { this.failGetReviewRequest += increment; }

    public int getSuccessRequest() {
        return successRequest;
    }

    public int getFailRequest() {
        return failRequest;
    }

    public int getSuccessGetReviewRequest() {
        return successGetReviewRequest;
    }

    public int getFailGetReviewRequest() {
        return failGetReviewRequest;
    }
}
