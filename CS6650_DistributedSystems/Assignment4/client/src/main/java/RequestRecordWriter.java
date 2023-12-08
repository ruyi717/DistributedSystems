import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RequestRecordWriter {
    private FileWriter fileWriter;
    private List<RequestRecord> records;
    private List<String> validIDs;

    public RequestRecordWriter(String filePath) {
        try {
            this.fileWriter = new FileWriter(filePath);
            this.fileWriter.append("startTime, requestType, latency, responseCode\n");
            this.records = new ArrayList<>();
            this.validIDs = new ArrayList<>();
        } catch (IOException e) {
            System.out.println("Failed to open file");
        }
    }

    public synchronized void addRequestRecord(RequestRecord requestRecord) {
        this.records.add(requestRecord);
    }

    public synchronized void addValidIDs(String id) { this.validIDs.add(id); }

    public synchronized String getValidID() {
        return this.validIDs.get(ThreadLocalRandom.current().nextInt(0, this.validIDs.size()));
    }

    public void writeRequestRecordToCSV(RequestRecord requestRecord) {
        try {
            this.fileWriter.append(requestRecord.toString() + "\n");
        } catch (IOException e) {
            System.out.println("Failed to write to file");
        }
    }

    private double getMedian(List<RequestRecord> requests) {
        if (requests.size() % 2 == 0) {
            return 0.5 * (requests.get(requests.size() / 2 - 1).getLatency() + requests.get(requests.size() / 2).getLatency());
        } else {
            return requests.get(requests.size() / 2).getLatency();
        }
    }

    private double getPercentile(int p, List<RequestRecord> requests) {
        return requests.get((int) 0.99 * p * requests.size()).getLatency();
    }

    public void calculateMetrics() {
        List<RequestRecord> gets = new ArrayList<>(), posts = new ArrayList<>();
        double getSum = 0;
        double postSum = 0;
        for (RequestRecord r : records) {
            if (r.getRequestType() == RequestRecord.RequestType.GET) {
                getSum += r.getLatency();
                gets.add(r);
            } else {
                postSum += r.getLatency();
                posts.add(r);
            }
//            writeRequestRecordToCSV(r);
        }
        double getMean = getSum / records.size();
        double postMean = postSum / records.size();
        Collections.sort(gets);
        Collections.sort(posts);
        double getMin = gets.get(0).getLatency(), getMax = gets.get(gets.size() - 1).getLatency();
        double postMin = posts.get(0).getLatency(), postMax = posts.get(posts.size() - 1).getLatency();
        double getMedian = getMedian(gets);
        double postMedian = getMedian(posts);
        double getPercent99 = getPercentile(99, gets);
        double postPercent99 = getPercentile(99, posts);
        System.out.println(
                "For all GET requests: \n"
                        +"Mean response time (millisecs): " + String.format("%.2f", getMean) + "\n"
                        +"Median response time (millisecs): " + String.format("%.2f", getMedian) + "\n"
                        +"99th percentile response time (millisecs): " + String.format("%.2f", getPercent99) + "\n"
                        +"Min response time (millisecs): " + String.format("%.2f", getMin) + "\n"
                        +"Max response time (millisecs): " + String.format("%.2f", getMax) + "\n"
        );
        System.out.println(
                "For all POST requests: \n"
                        +"Mean response time (millisecs): " + String.format("%.2f", postMean) + "\n"
                        +"Median response time (millisecs): " + String.format("%.2f", postMedian) + "\n"
                        +"99th percentile response time (millisecs): " + String.format("%.2f", postPercent99) + "\n"
                        +"Min response time (millisecs): " + String.format("%.2f", postMin) + "\n"
                        +"Max response time (millisecs): " + String.format("%.2f", postMax)
        );
    }
}
