package com.student.records.controller;

import com.student.records.domain.NodeStatus;
import com.student.records.services.HealthMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cluster")
@CrossOrigin(origins = "*")
public class HealthController {

    @Autowired
    private com.student.records.distributed.NodeHealthMonitor nodeHealthMonitor;

    @Autowired
    private com.student.records.distributed.LeaderElection leaderElection;

    @GetMapping("/health")
    public ResponseEntity<?> getHealth() {
        List<Map<String, String>> nodes = new java.util.ArrayList<>();
        String leader = leaderElection.getCurrentLeader();

        nodes.add(createNodeMap("node1", nodeHealthMonitor.isNode1Up(), "node1".equals(leader)));
        nodes.add(createNodeMap("node2", nodeHealthMonitor.isNode2Up(), "node2".equals(leader)));
        nodes.add(createNodeMap("node3", nodeHealthMonitor.isNode3Up(), "node3".equals(leader)));

        Map<String, Object> response = new HashMap<>();
        response.put("nodes", nodes);
        return ResponseEntity.ok(response);
    }

    private Map<String, String> createNodeMap(String id, boolean isUp, boolean isLeader) {
        Map<String, String> map = new HashMap<>();
        map.put("nodeId", id);
        map.put("role", isLeader ? "LEADER" : "REPLICA");
        map.put("status", isUp ? "ACTIVE" : "FAILED");
        map.put("lastHeartbeat", isUp ? "Live Network Ping OK" : "Timeout > 10s");
        return map;
    }

    // Legacy Simulation endpoints (Disabled since we now use real network failovers)
    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat() { return ResponseEntity.ok().build(); }

    @PostMapping("/simulate-failure")
    public ResponseEntity<?> simulateFailure() { return ResponseEntity.ok().build(); }
}
