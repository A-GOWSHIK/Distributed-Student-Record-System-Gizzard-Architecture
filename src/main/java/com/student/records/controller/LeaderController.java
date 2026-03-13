package com.student.records.controller;

import com.student.records.distributed.LeaderElection;
import com.student.records.distributed.LogicalClock;
import com.student.records.distributed.RecoveryManager;
import com.student.records.distributed.NodeHealthMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Leader Election status endpoint.
 * GET /cluster/leader — returns current leader and cluster health summary.
 */
@RestController
@CrossOrigin(origins = "*")
public class LeaderController {

    @Autowired
    private LeaderElection leaderElection;

    @Autowired
    private LogicalClock logicalClock;

    @Autowired
    private RecoveryManager recoveryManager;

    @Autowired
    private NodeHealthMonitor nodeHealthMonitor;

    @Value("${node.id}")
    private String nodeId;

    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/cluster/leader")
    public Map<String, Object> getLeader() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("currentLeader",    leaderElection.getCurrentLeader());
        result.put("isThisNodeLeader", leaderElection.isLeader());
        result.put("thisNode",         nodeId);
        result.put("lamportClock",     logicalClock.getTime());
        result.put("node2Up",          nodeHealthMonitor.isNode2Up());
        result.put("node3Up",          nodeHealthMonitor.isNode3Up());
        result.put("recoveryStatus",   recoveryManager.getRecoveryStatus());
        System.out.println("[LeaderElection] /cluster/leader queried — leader=" + leaderElection.getCurrentLeader()
                + " lamport=" + logicalClock.getTime());
        return result;
    }
}
