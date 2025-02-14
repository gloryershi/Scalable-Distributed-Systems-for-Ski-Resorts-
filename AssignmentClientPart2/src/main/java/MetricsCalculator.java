import java.util.List;

public class MetricsCalculator {
    public static void calculateAndDisplayMetrics(List<RequestRecord> requestRecords) {
        List<Long> latencies = requestRecords.stream()
                .map(record -> record.latency)
                .sorted()
                .toList();

        long totalLatency = latencies.stream().mapToLong(Long::longValue).sum();
        double meanLatency = totalLatency / (double) latencies.size();
        double medianLatency = latencies.get(latencies.size() / 2);
        long minLatency = latencies.get(0);
        long maxLatency = latencies.get(latencies.size() - 1);
        long p99Latency = latencies.get((int) (latencies.size() * 0.99));

        System.out.println("Mean response time: " + meanLatency + " ms");
        System.out.println("Median response time: " + medianLatency + " ms");
        System.out.println("Min response time: " + minLatency + " ms");
        System.out.println("Max response time: " + maxLatency + " ms");
        System.out.println("P99 response time: " + p99Latency + " ms");
    }
}