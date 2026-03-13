package com.student.records.distributed;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple token-based Distributed Lock with 3-second timeout.
 * In a real system this would use Redis or ZooKeeper; here it
 * provides the interface and protocol for educational purposes.
 */
@Component
public class DistributedLock {

    private final AtomicBoolean locked = new AtomicBoolean(false);
    private final AtomicLong lockAcquiredAt = new AtomicLong(0);
    private volatile String lockHolder = null;

    private static final long TIMEOUT_MS = 3000;

    /**
     * Acquire lock for a given node.
     * Returns true if lock successfully acquired, false if already held.
     * Automatically releases expired locks.
     */
    public synchronized boolean acquire(String nodeId) {
        // Auto-release expired lock
        if (locked.get() && System.currentTimeMillis() - lockAcquiredAt.get() > TIMEOUT_MS) {
            System.out.println("[Lock] Lock timeout — auto-releasing from " + lockHolder);
            release(lockHolder);
        }

        if (!locked.get()) {
            locked.set(true);
            lockHolder = nodeId;
            lockAcquiredAt.set(System.currentTimeMillis());
            System.out.println("[Lock] " + nodeId + " acquired lock at t=" + lockAcquiredAt.get());
            return true;
        }

        System.out.println("[Lock] " + nodeId + " could not acquire lock — held by " + lockHolder);
        return false;
    }

    /** Release the lock. Only the holder can release it. */
    public synchronized boolean release(String nodeId) {
        if (locked.get() && nodeId != null && nodeId.equals(lockHolder)) {
            locked.set(false);
            lockHolder = null;
            System.out.println("[Lock] " + nodeId + " released lock");
            return true;
        }
        System.out.println("[Lock] Release failed — lock held by " + lockHolder + ", requested by " + nodeId);
        return false;
    }

    public boolean isLocked() { return locked.get(); }
    public String getLockHolder() { return lockHolder; }
}
