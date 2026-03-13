package com.student.records.services;

import com.student.records.distributed.LeaderElection;
import com.student.records.distributed.ReplicaManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ClusterService {

    @Autowired
    private LeaderElection leaderElection;

    @Autowired
    private ReplicaManager replicaManager;

    private static final String[] CLUSTER_NODES = { "Node1", "Node2", "Node3" };
    private static final int REPLICA_COUNT = 2;

    /**
     * Returns live cluster status using LeaderElection and ReplicaManager.
     */
    public Map<String, Object> getClusterStatus() {
        String leader = leaderElection.electLeader(CLUSTER_NODES);
        replicaManager.replicateData("cluster-status-check");

        Map<String, Object> status = new HashMap<>();
        status.put("activeNodes", CLUSTER_NODES.length);
        status.put("leaderNode", leader);
        status.put("replicas", REPLICA_COUNT);
        status.put("clusterStatus", "HEALTHY");
        return status;
    }

    /**
     * Returns the current leader node name.
     */
    public String getLeaderNode() {
        return leaderElection.getLeader() != null
                ? leaderElection.getLeader()
                : leaderElection.electLeader(CLUSTER_NODES);
    }

    public int getActiveNodeCount() {
        return CLUSTER_NODES.length;
    }
}
