public class RequestCounter {
    private int successRequest;
    private int failRequest;

    public RequestCounter() {
        this.successRequest = 0;
        this.failRequest = 0;
    }

    public synchronized void incrementSuccessRequest(int increment) {
        this.successRequest += increment;
    }

    public synchronized void incrementFailRequest(int increment) {
        this.failRequest += increment;
    }

    public int getSuccessRequest() {
        return successRequest;
    }

    public int getFailRequest() {
        return failRequest;
    }
}
