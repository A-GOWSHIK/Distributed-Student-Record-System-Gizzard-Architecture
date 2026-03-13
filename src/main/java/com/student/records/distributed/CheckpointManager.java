package com.student.records.distributed;

import com.student.records.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Chandy-Lamport Snapshot Manager.
 *
 * Protocol:
 *   1. Initiating node records its local state.
 *   2. Sends SNAPSHOT marker to all other nodes via POST /snapshot/start.
 *   3. Each node records local students + Lamport clock value.
 *   4. Snapshot is stored in memory and retrievable via GET /snapshot/state.
 */
@Service
public class CheckpointManager {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private LogicalClock logicalClock;

    @Value("${node.id}")
    private String nodeId;

    /** In-memory snapshot storage */
    private volatile Map<String, Object> lastSnapshot = null;

    /**
     * Record local state as a snapshot.
     * Called by initiating node AND by nodes that received the marker.
     */
    public Map<String, Object> takeLocalSnapshot() {
        long ts = logicalClock.tick();
        long studentCount = studentRepository.count();

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("nodeId",        nodeId);
        snapshot.put("lamportClock",  ts);
        snapshot.put("studentCount",  studentCount);
        snapshot.put("timestamp",     LocalDateTime.now().toString());
        snapshot.put("status",        "SNAPSHOT_COMPLETE");

        this.lastSnapshot = snapshot;
        System.out.println("[Snapshot] Local state recorded: nodeId=" + nodeId
                + " lamport=" + ts + " students=" + studentCount);
        return snapshot;
    }

    /** Returns the most recent snapshot, or empty state if never snapshotted. */
    public Map<String, Object> getLastSnapshot() {
        if (lastSnapshot == null) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("nodeId",       nodeId);
            empty.put("lamportClock", logicalClock.getTime());
            empty.put("studentCount", 0);
            empty.put("status",       "NO_SNAPSHOT_YET");
            return empty;
        }
        return lastSnapshot;
    }
}
