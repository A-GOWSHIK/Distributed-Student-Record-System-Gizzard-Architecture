package com.student.records.distributed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Fault Recovery Manager.
 *
 * Periodically checks node health and logs failover decisions.
 * Works with NodeHealthMonitor for live status.
 *
 * Recovery strategy (ring failover):
 *   If Node2 is DOWN → Node1 handles shard1 (odd IDs)
 *   If Node3 is DOWN → Node1 handles shard2 (id%3==2)
 *   If both are DOWN → Node1 handles all shards
 */
@Service
public class RecoveryManager {

    @Autowired
    private NodeHealthMonitor nodeHealthMonitor;

    @Value("${node.id}")
    private String nodeId;

    @Value("${server.port}")
    private String serverPort;

    @Scheduled(fixedRate = 5000)
    public void checkAndRecover() {
        boolean node1Up = nodeHealthMonitor.isNode1Up();
        boolean node2Up = nodeHealthMonitor.isNode2Up();
        boolean node3Up = nodeHealthMonitor.isNode3Up();

        if (!node1Up || !node2Up || !node3Up) {
            System.out.println("[Recovery] Cluster degraded. N1:" + node1Up + " N2:" + node2Up + " N3:" + node3Up);
        }
    }

    /** Summary for UI display */
    public String getRecoveryStatus() {
        boolean n1 = nodeHealthMonitor.isNode1Up();
        boolean n2 = nodeHealthMonitor.isNode2Up();
        boolean n3 = nodeHealthMonitor.isNode3Up();
        if (n1 && n2 && n3) return "ALL NODES HEALTHY";
        return "DEGRADED: " + (!n1?"Node1 ":"") + (!n2?"Node2 ":"") + (!n3?"Node3":"") + "DOWN";
    }

    /** Called by HealthMonitorService and NodeFailureService after a failure event. */
    public void recoverSystem() {
        System.out.println("[Recovery] recoverSystem() triggered — checking cluster state");
        checkAndRecover();
    }
}
