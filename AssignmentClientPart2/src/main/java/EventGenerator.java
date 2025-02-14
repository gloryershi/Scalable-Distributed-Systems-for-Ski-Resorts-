import java.util.concurrent.BlockingQueue;

public class EventGenerator implements Runnable {

    private final BlockingQueue<LiftRideEvent> eventQueue;
    private final int totalEvents;

    public EventGenerator(BlockingQueue<LiftRideEvent> eventQueue, int totalEvents) {
        this.eventQueue = eventQueue;
        this.totalEvents = totalEvents;
    }

    @Override
    public void run() {
        for (int i = 0; i < totalEvents; i++) {
            LiftRideEvent event = new LiftRideEvent(
                    (int) (Math.random() * 100_000) + 1, // skierID: 1 到 100,000
                    (int) (Math.random() * 10) + 1,      // resortID: 1 到 10
                    (int) (Math.random() * 40) + 1,      // liftID: 1 到 40
                    2025,                                // seasonID: 固定为 2025
                    1,                                   // dayID: 固定为 1
                    (int) (Math.random() * 360) + 1      // time: 1 到 360
            );
            eventQueue.add(event); // 将事件放入队列
        }
    }
}