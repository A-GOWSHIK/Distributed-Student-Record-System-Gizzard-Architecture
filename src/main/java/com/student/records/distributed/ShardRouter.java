package com.student.records.distributed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShardRouter {

    public static final String NODE1 = "http://10.48.29.58:8081";
    public static final String NODE2 = "http://10.48.29.166:8082";
    public static final String NODE3 = "http://10.48.29.228:8083";

    @Autowired
    private NodeHealthMonitor nodeHealthMonitor;

    /**
     * 3-node shard routing: id % 3
     *   0 → Node1 (ANBARASAN)
     *   1 → Node2 (CHARA)
     *   2 → Node3 (NSNSReddy)
     * Failover: if target is DOWN, route to Node1.
     */
    public String getShard(int studentId) {
        int shard = studentId % 3;
        if (shard == 0) {
            System.out.println("[ROUTER] Routing student " + studentId + " → Node1 (shard0)");
            return NODE1;
        } else if (shard == 1) {
            if (!nodeHealthMonitor.isNode2Up()) {
                System.out.println("[FAILOVER] Node2 down — routing student " + studentId + " to Node1");
                return NODE1;
            }
            System.out.println("[ROUTER] Routing student " + studentId + " → Node2 (shard1)");
            return NODE2;
        } else {
            if (!nodeHealthMonitor.isNode3Up()) {
                System.out.println("[FAILOVER] Node3 down — routing student " + studentId + " to Node1");
                return NODE1;
            }
            System.out.println("[ROUTER] Routing student " + studentId + " → Node3 (shard2)");
            return NODE3;
        }
    }

    /**
     * Ring replication: each shard replicates to the next node.
     *   shard0 (Node1) → replica on Node2
     *   shard1 (Node2) → replica on Node3
     *   shard2 (Node3) → replica on Node1
     */
    public String getReplicaBase(int studentId) {
        int shard = studentId % 3;
        if (shard == 0) return NODE2;
        if (shard == 1) return NODE3;
        return NODE1;
    }
}
