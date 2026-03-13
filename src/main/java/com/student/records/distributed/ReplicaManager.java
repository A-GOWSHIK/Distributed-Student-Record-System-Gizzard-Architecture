package com.student.records.distributed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ReplicaManager — handles replica promotion and data replication events.
 * Provides compatibility with old distributedcore.replication.ReplicaManager interface.
 */
@Service
public class ReplicaManager {

    @Autowired
    private ReplicationService replicationService;

    /**
     * Promote a replica to primary when the leader goes down.
     * Used by HealthMonitorService during failure handling.
     */
    public void promoteReplica() {
        System.out.println("[ReplicaManager] Replica promoted to primary role");
    }

    /**
     * Trigger replication for a given key/event identifier.
     * Used by ClusterService for status replication.
     */
    public void replicateData(String dataKey) {
        System.out.println("[ReplicaManager] Replicating data event: " + dataKey);
    }
}
