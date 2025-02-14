import java.util.concurrent.BlockingQueue;

public class EventGenerator {
    public static void generateEvents(int totalRequests, BlockingQueue<LiftRideEvent> eventQueue) {
        for (int i = 0; i < totalRequests; i++) {
            try {
                eventQueue.put(new LiftRideEvent(
                        (int) (Math.random() * 100_000) + 1,
                        (int) (Math.random() * 10) + 1,
                        (int) (Math.random() * 40) + 1,
                        2025,
                        1,
                        (int) (Math.random() * 360) + 1
                ));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
