package com.student.records.services;

import com.student.records.distributed.RecoveryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class NodeFailureService {

    @Autowired
    private RecoveryManager recoveryManager;

    @Autowired
    private SystemLogService systemLogService;

    /**
     * Simulates a node failure, triggers recovery, and logs all events.
     */
    public Map<String, String> simulateNodeFailure(String nodeName) {
        System.out.println("[NodeFailure] " + nodeName + " failed");
        systemLogService.log(nodeName + " failed - node failure detected", nodeName);

        System.out.println("[NodeFailure] Promoting replica for " + nodeName);
        systemLogService.log("Replica promoted after failure of " + nodeName, "Node1");

        recoveryManager.recoverSystem();
        systemLogService.log("Recovery started for " + nodeName + " - system restored", "Node1");

        System.out.println("[NodeFailure] System recovered after " + nodeName + " failure");

        Map<String, String> result = new HashMap<>();
        result.put("failedNode", nodeName);
        result.put("replicaPromoted", "true");
        result.put("recoveryStatus", "RECOVERED");
        result.put("message", nodeName + " failed → Replica promoted → System recovered");
        return result;
    }
}
