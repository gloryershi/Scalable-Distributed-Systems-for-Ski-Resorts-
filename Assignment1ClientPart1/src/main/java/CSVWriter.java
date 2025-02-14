import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVWriter {
    public static void writeRecordsToCSV(List<RequestRecord> requestRecords, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("StartTime,RequestType,Latency,ResponseCode\n");
            for (RequestRecord record : requestRecords) {
                writer.write(record.startTime + "," + record.requestType + "," + record.latency + "," + record.responseCode + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
