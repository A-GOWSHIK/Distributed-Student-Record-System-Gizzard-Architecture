package com.student.records.controller;

import com.student.records.distributed.CheckpointManager;
import com.student.records.distributed.RpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Chandy-Lamport Snapshot endpoints.
 * POST /snapshot/start — initiates global snapshot (propagates marker to other nodes)
 * GET /snapshot/state — returns this node's latest snapshot
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/snapshot")
public class SnapshotController {

    @Autowired
    private CheckpointManager checkpointManager;

    @Autowired
    private RpcClient rpcClient;

    @Value("${server.port}")
    private String serverPort;

    private static final String[] OTHER_NODES = {
        "http://10.48.29.58:8081",
        "http://10.48.29.166:8082",
        "http://10.48.29.228:8083"
    };

    /**
     * Start a global snapshot.
     * 1. Record local state.
     * 2. Send SNAPSHOT marker to other nodes.
     */
    @PostMapping("/start")
    public Map<String, Object> startSnapshot(
            @RequestHeader(value = "X-Snapshot-Marker", required = false) String marker) {

        System.out.println("[Snapshot] Snapshot initiated on port " + serverPort);

        // Record local state
        Map<String, Object> localState = checkpointManager.takeLocalSnapshot();

        // Propagate marker to other nodes (only if this is the initiator, not a marker)
        if (marker == null) {
            List<Map<String, Object>> remoteStates = new ArrayList<>();
            for (String node : OTHER_NODES) {
                if (!node.contains(serverPort)) {
                    try {
                        System.out.println("[Snapshot] Sending marker to " + node);
                        rpcClient.post(node + "/snapshot/start", null);
                    } catch (Exception e) {
                        System.out.println("[Snapshot] Node unreachable: " + node);
                    }
                }
            }
        }

        return localState;
    }

    /** Return this node's latest snapshot state. */
    @GetMapping("/state")
    public Map<String, Object> getSnapshotState() {
        return checkpointManager.getLastSnapshot();
    }
}
