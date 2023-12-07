import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequestRecordWriter {
    private FileWriter fileWriter;
    private List<RequestRecord> records;

    public RequestRecordWriter(String filePath) {
        try {
            this.fileWriter = new FileWriter(filePath);
            this.fileWriter.append("startTime, requestType, latency, responseCode\n");
            this.records = new ArrayList<>();
        } catch (IOException e) {
            System.out.println("Failed to open file");
        }
    }

    public synchronized void addRequestRecord(RequestRecord requestRecord) {
        this.records.add(requestRecord);
    }

    public void writeRequestRecordToCSV(RequestRecord requestRecord) {
        try {
            this.fileWriter.append(requestRecord.toString() + "\n");
        } catch (IOException e) {
            System.out.println("Failed to write to file");
        }
    }

    private double getMedian() {
        if (records.size() % 2 == 0) {
            return 0.5 * (records.get(records.size() / 2 - 1).getLatency() + records.get(records.size() / 2).getLatency());
        } else {
            return records.get(records.size() / 2).getLatency();
        }
    }

    private double getPercentile(int p) {
        return records.get((int) 0.99 * p * records.size()).getLatency();
    }

    public void calculateMetrics() {
        double sum = 0;
        for (RequestRecord r : records) {
            sum += r.getLatency();
            writeRequestRecordToCSV(r);
        }
        double mean = sum / records.size();
        Collections.sort(records);
        double min = records.get(0).getLatency(), max = records.get(records.size() - 1).getLatency();
        double median = getMedian();
        double percent99 = getPercentile(99);
        System.out.println(
                        "Mean response time (millisecs): " + String.format("%.2f", mean) + "\n"
                        +"Median response time (millisecs): " + String.format("%.2f", median) + "\n"
                        +"99th percentile response time (millisecs): " + String.format("%.2f", percent99) + "\n"
                        +"Min response time (millisecs): " + String.format("%.2f", min) + "\n"
                        +"Max response time (millisecs): " + String.format("%.2f", max)
        );
    }
}
