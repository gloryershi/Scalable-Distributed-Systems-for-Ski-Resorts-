import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SkierClientPart2 {
    private static final int TOTAL_REQUESTS = 200_000;
    private static final int INITIAL_THREADS = 32;
    private static final int REQUESTS_PER_THREAD = 1000;
    private static final int MAX_RETRIES = 5;
    private static final int MAX_THREAD_POOL_SIZE = 150;

    private static final String SERVER_BASE_URL = "http://localhost:8080/Assignment1Server_war_exploded";

    private static final AtomicInteger successfulRequests = new AtomicInteger(0);
    private static final AtomicInteger failedRequests = new AtomicInteger(0);

    private static final BlockingQueue<LiftRideEvent> eventQueue = new LinkedBlockingQueue<>();
    private static final List<RequestRecord> requestRecords = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        generateEvents();

        List<Thread> initialThreads = new ArrayList<>();
        RequestSender requestSender = new RequestSender(eventQueue, successfulRequests, failedRequests, SERVER_BASE_URL, MAX_RETRIES);

        for (int i = 0; i < INITIAL_THREADS; i++) {
            Thread thread = new Thread(() -> requestSender.sendRequests(REQUESTS_PER_THREAD));
            initialThreads.add(thread);
            thread.start();
        }

        for (Thread thread : initialThreads) {
            thread.join();
        }

        System.out.println("Initial 32 threads completed, starting thread pool...");

        ExecutorService executorService = new ThreadPoolExecutor(
                MAX_THREAD_POOL_SIZE,
                MAX_THREAD_POOL_SIZE,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        int remainingRequests = TOTAL_REQUESTS - INITIAL_THREADS * REQUESTS_PER_THREAD;
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < remainingRequests; i++) {
            tasks.add(() -> { requestSender.sendRequests(1); return null; });
        }
        executorService.invokeAll(tasks);

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Client Configuration:");
        System.out.println("Total Requests: " + TOTAL_REQUESTS);
        System.out.println("Initial Threads: " + INITIAL_THREADS);
        System.out.println("Requests per Initial Thread: " + REQUESTS_PER_THREAD);
        System.out.println("Max Thread Pool Size: " + MAX_THREAD_POOL_SIZE);

        System.out.println("Successful requests: " + successfulRequests.get());
        System.out.println("Failed requests: " + failedRequests.get());
        System.out.println("Total run time: " + totalTime + " ms");
        System.out.println("Throughput: " + (TOTAL_REQUESTS / (totalTime / 1000.0)) + " requests per second");

        CsvWriter.writeRecordsToCSV(requestRecords, "request_records.csv");
        MetricsCalculator.calculateAndDisplayMetrics(requestRecords);
    }

    private static void generateEvents() {
        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            eventQueue.add(new LiftRideEvent(
                    (int) (Math.random() * 100_000) + 1,
                    (int) (Math.random() * 10) + 1,
                    (int) (Math.random() * 40) + 1,
                    2025,
                    1,
                    (int) (Math.random() * 360) + 1
            ));
        }
    }

    public static void addRequestRecord(RequestRecord record) {
        requestRecords.add(record);
    }
}