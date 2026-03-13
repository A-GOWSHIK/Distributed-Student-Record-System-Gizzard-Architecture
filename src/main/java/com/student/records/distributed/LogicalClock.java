package com.student.records.distributed;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Lamport Logical Clock — provides total event ordering across nodes.
 * Rules:
 *   1. Before sending a message: tick() and attach timestamp.
 *   2. On receiving a message: update(receivedTs) to sync clock.
 *   3. Local event: tick().
 */
@Component
public class LogicalClock {

    private final AtomicLong clock = new AtomicLong(0);

    /** Increment before a local event or sending a message. */
    public long tick() {
        long ts = clock.incrementAndGet();
        System.out.println("[Lamport] clock ticked → " + ts);
        return ts;
    }

    /**
     * On receiving a message with remote timestamp:
     * clock = max(local, remote) + 1
     */
    public long update(long receivedTimestamp) {
        long updated = clock.updateAndGet(local ->
                Math.max(local, receivedTimestamp) + 1);
        System.out.println("[Lamport] clock updated on receive (remote=" + receivedTimestamp + ") → " + updated);
        return updated;
    }

    /** Current clock value without incrementing. */
    public long getTime() {
        return clock.get();
    }
}
