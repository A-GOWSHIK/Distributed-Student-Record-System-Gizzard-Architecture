package com.student.records.controller;

import com.student.records.domain.Student;
import com.student.records.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles replication RPC calls from the primary shard node.
 * Records are saved directly to the local DB — NO re-routing, NO re-replication.
 * This prevents replication loops.
 */
@RestController
@CrossOrigin(origins = "*")
public class ReplicaController {

    @Autowired
    private StudentRepository studentRepository;

    @Value("${node.id}")
    private String nodeId;

    @Autowired
    private com.student.records.distributed.LogicalClock logicalClock;

    private void syncClock(String lamportHeader) {
        if (lamportHeader != null) {
            try {
                logicalClock.update(Long.parseLong(lamportHeader));
            } catch (NumberFormatException ignored) {}
        }
    }

    /**
     * Health ping endpoint used by NodeHealthMonitor on Node1 to detect Node2 liveness.
     */
    @GetMapping("/replica/ping")
    public ResponseEntity<String> ping(@RequestHeader(value = "X-Lamport-Timestamp", required = false) String timestamp) {
        syncClock(timestamp);
        return ResponseEntity.ok("ALIVE:" + nodeId);
    }

    /**
     * Accept a replicated student record from the primary shard.
     * Save directly — skip routing and replication to avoid loops.
     */
    @PostMapping("/replica/students")
    public ResponseEntity<?> receiveReplica(@RequestBody Student student,
                                            @RequestHeader(value = "X-Lamport-Timestamp", required = false) String timestamp) {
        syncClock(timestamp);
        System.out.println("[REPLICATION] " + nodeId + " received replica for student id=" + student.getId());
        Student saved = studentRepository.save(student);
        System.out.println("[REPLICATION] Replica saved locally on " + nodeId + " — student id=" + saved.getId());
        
        // Return our updated clock in response
        return ResponseEntity.ok()
                .header("X-Lamport-Timestamp", String.valueOf(logicalClock.getTime()))
                .body(saved);
    }
}
