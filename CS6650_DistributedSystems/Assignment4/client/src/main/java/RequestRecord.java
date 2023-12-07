public class RequestRecord implements Comparable<RequestRecord> {
    enum RequestType {
        POST, GET
    }
    private long startTime;
    private RequestType requestType;
    private long latency;
    private int responseCode;

    public RequestRecord(long startTime, RequestType requestType, long latency, int responseCode) {
        this.startTime = startTime;
        this.requestType = requestType;
        this.latency = latency;
        this.responseCode = responseCode;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public String toString() {
        return startTime + "," + requestType + "," + latency + "," + responseCode;
    }

    @Override
    public int compareTo(RequestRecord o) {
        return (int) (this.getLatency() - o.getLatency());
    }
}
