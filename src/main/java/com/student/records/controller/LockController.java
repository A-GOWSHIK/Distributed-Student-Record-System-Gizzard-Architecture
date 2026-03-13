package com.student.records.controller;

import com.student.records.distributed.DistributedLock;
import com.student.records.distributed.LogicalClock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Distributed Lock endpoints.
 * POST /lock/acquire?nodeId=node1 — acquire lock
 * POST /lock/release?nodeId=node1 — release lock
 * GET  /lock/status                — current lock state
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/lock")
public class LockController {

    @Autowired
    private DistributedLock distributedLock;

    @Autowired
    private LogicalClock logicalClock;

    @Value("${node.id}")
    private String nodeId;

    @PostMapping("/acquire")
    public ResponseEntity<Map<String, Object>> acquire(
            @RequestParam(defaultValue = "") String requestingNode) {
        String requester = requestingNode.isEmpty() ? nodeId : requestingNode;
        logicalClock.tick();
        boolean success = distributedLock.acquire(requester);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("acquired",    success);
        result.put("holder",      distributedLock.getLockHolder());
        result.put("lamportClock", logicalClock.getTime());
        result.put("message",     success ? requester + " acquired lock" : "Lock held by " + distributedLock.getLockHolder());
        return success ? ResponseEntity.ok(result) : ResponseEntity.status(409).body(result);
    }

    @PostMapping("/release")
    public ResponseEntity<Map<String, Object>> release(
            @RequestParam(defaultValue = "") String requestingNode) {
        String requester = requestingNode.isEmpty() ? nodeId : requestingNode;
        logicalClock.tick();
        boolean success = distributedLock.release(requester);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("released",    success);
        result.put("lamportClock", logicalClock.getTime());
        result.put("message",     success ? requester + " released lock" : "Release failed — not the lock holder");
        return success ? ResponseEntity.ok(result) : ResponseEntity.status(403).body(result);
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("locked",      distributedLock.isLocked());
        result.put("holder",      distributedLock.getLockHolder());
        result.put("lamportClock", logicalClock.getTime());
        return result;
    }
}
