import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestSender {
    private static final String SERVER_BASE_URL = "http://[EC2-ip]:8080/Assignment1Server_war"; //change the [EC2ip] to your EC2IP
    private static final int MAX_RETRIES = 5;
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    public static void sendRequests(int requestCount, BlockingQueue<LiftRideEvent> eventQueue,
                                    AtomicInteger successfulRequests, AtomicInteger failedRequests,
                                    List<RequestRecord> requestRecords) {
        for (int i = 0; i < requestCount; i++) {
            try {
                LiftRideEvent event = eventQueue.take();
                String jsonBody = gson.toJson(event);
                String url = String.format("%s/skiers/%d/seasons/%d/days/%d/skiers/%d",
                        SERVER_BASE_URL, event.getResortID(), event.getSeasonID(), event.getDayID(), event.getSkierID());

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                int retries = 0;
                boolean success = false;
                int responseCode = 0;
                long startTime = System.currentTimeMillis();

                while (retries < MAX_RETRIES && !success) {
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    responseCode = response.statusCode();

                    if (responseCode == 201) {
                        successfulRequests.incrementAndGet();
                        success = true;
                    } else if (responseCode == 429 || responseCode >= 500) {
                        retries++;
                        if (retries == MAX_RETRIES) {
                            failedRequests.incrementAndGet();
                        } else {
                            Thread.sleep(100 * retries);
                        }
                    } else {
                        failedRequests.incrementAndGet();
                        break;
                    }
                }


                long endTime = System.currentTimeMillis();
                long latency = endTime - startTime;
                requestRecords.add(new RequestRecord(startTime, "POST", latency, responseCode));

            } catch (Exception e) {
                e.printStackTrace();
                failedRequests.incrementAndGet();
            }
        }
    }
}
