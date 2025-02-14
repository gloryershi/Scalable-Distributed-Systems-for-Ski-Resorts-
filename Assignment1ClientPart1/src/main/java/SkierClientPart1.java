import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SkierClientPart1 {
    private static final int TOTAL_REQUESTS = 200_000;
    private static final int INITIAL_THREADS = 32;
    private static final int REQUESTS_PER_THREAD = 1000;
    private static final int MAX_THREAD_POOL_SIZE = 150;

    private static final AtomicInteger successfulRequests = new AtomicInteger(0);
    private static final AtomicInteger failedRequests = new AtomicInteger(0);

    private static final BlockingQueue<LiftRideEvent> eventQueue = new LinkedBlockingQueue<>();
    private static final List<RequestRecord> requestRecords = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        // 生成事件
        EventGenerator.generateEvents(TOTAL_REQUESTS, eventQueue);

        // 32个线程并发执行
        List<Thread> initialThreads = new ArrayList<>();
        for (int i = 0; i < INITIAL_THREADS; i++) {
            Thread thread = new Thread(() -> RequestSender.sendRequests(REQUESTS_PER_THREAD, eventQueue, successfulRequests, failedRequests, requestRecords));
            initialThreads.add(thread);
            thread.start();
        }

        for (Thread thread : initialThreads) {
            thread.join();
        }


        System.out.println("Initial 32 threads completed, starting thread pool...");

        // 使用线程池处理剩余请求
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREAD_POOL_SIZE);
        int remainingRequests = TOTAL_REQUESTS - INITIAL_THREADS * REQUESTS_PER_THREAD;
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < remainingRequests; i++) {
            tasks.add(() -> {
                RequestSender.sendRequests(1, eventQueue, successfulRequests, failedRequests, requestRecords);
                return null;
            });
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

        CSVWriter.writeRecordsToCSV(requestRecords, "request_records.csv");

    }
}
