import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestSender {
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    private final BlockingQueue<LiftRideEvent> eventQueue;
    private final AtomicInteger successfulRequests;
    private final AtomicInteger failedRequests;
    private final String serverBaseUrl;
    private final int maxRetries;

    public RequestSender(BlockingQueue<LiftRideEvent> eventQueue, AtomicInteger successfulRequests,
                         AtomicInteger failedRequests, String serverBaseUrl, int maxRetries) {
        this.eventQueue = eventQueue;
        this.successfulRequests = successfulRequests;
        this.failedRequests = failedRequests;
        this.serverBaseUrl = serverBaseUrl;
        this.maxRetries = maxRetries;
    }

    public void sendRequests(int requestCount) {
        for (int i = 0; i < requestCount; i++) {
            try {
                LiftRideEvent event = eventQueue.take();
                String jsonBody = gson.toJson(event);
                String url = String.format("%s/skiers/%d/seasons/%d/days/%d/skiers/%d",
                        serverBaseUrl, event.getResortID(), event.getSeasonID(), event.getDayID(), event.getSkierID());

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                int retries = 0;
                boolean success = false;
                int responseCode = 0;
                long startTime = System.currentTimeMillis();

                while (retries < maxRetries && !success) {
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    responseCode = response.statusCode();

                    if (responseCode == 201) {
                        successfulRequests.incrementAndGet();
                        success = true;
                    } else if (responseCode >= 400 && responseCode < 500) {
                        failedRequests.incrementAndGet();
                        break;
                    } else if (responseCode >= 500) {
                        retries++;
                        if (retries == maxRetries) {
                            failedRequests.incrementAndGet();
                        }
                    }
                }

                long endTime = System.currentTimeMillis();
                long latency = endTime - startTime;

                SkierClientPart2.addRequestRecord(new RequestRecord(startTime, "POST", latency, responseCode));

            } catch (Exception e) {
                e.printStackTrace();
                failedRequests.incrementAndGet();
            }
        }
    }
}