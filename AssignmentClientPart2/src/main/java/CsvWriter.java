import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvWriter {
    public static void writeRecordsToCSV(List<RequestRecord> requestRecords, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("StartTime,RequestType,Latency,ResponseCode\n");
            for (RequestRecord record : requestRecords) {
                writer.write(record.startTime + "," + record.requestType + "," + record.latency + "," + record.responseCode + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}