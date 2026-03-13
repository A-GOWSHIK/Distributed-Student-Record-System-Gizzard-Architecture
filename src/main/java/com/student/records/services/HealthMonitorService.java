package com.student.records.services;

import com.student.records.domain.NodeStatus;
import com.student.records.repository.NodeStatusRepository;
import com.student.records.distributed.RecoveryManager;
import com.student.records.distributed.ReplicaManager;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HealthMonitorService {

    private final NodeStatusRepository nodeStatusRepository;
    private final RecoveryManager recoveryManager;
    private final ReplicaManager replicaManager;
    private final SystemLogService systemLogService;

    public HealthMonitorService(NodeStatusRepository nodeStatusRepository,
            RecoveryManager recoveryManager,
            ReplicaManager replicaManager,
            SystemLogService systemLogService) {
        this.nodeStatusRepository = nodeStatusRepository;
        this.recoveryManager = recoveryManager;
        this.replicaManager = replicaManager;
        this.systemLogService = systemLogService;
    }

    @PostConstruct
    public void initNodes() {
        if (nodeStatusRepository.count() == 0) {
            nodeStatusRepository.save(new NodeStatus("Node1", "ACTIVE", LocalDateTime.now(), "LEADER"));
            nodeStatusRepository.save(new NodeStatus("Node2", "ACTIVE", LocalDateTime.now(), "REPLICA"));
            nodeStatusRepository.save(new NodeStatus("Node3", "ACTIVE", LocalDateTime.now(), "REPLICA"));
            systemLogService.log("Cluster initialized with 3 nodes", "SYSTEM");
        }
    }

    public List<NodeStatus> getClusterHealth() {
        // Simple failure detection check before returning
        detectFailures();
        return nodeStatusRepository.findAll();
    }

    public void simulateHeartbeat(String nodeId) {
        Optional<NodeStatus> nodeOpt = nodeStatusRepository.findById(nodeId);
        if (nodeOpt.isPresent()) {
            NodeStatus node = nodeOpt.get();
            node.setLastHeartbeat(LocalDateTime.now());
            node.setStatus("ACTIVE");
            nodeStatusRepository.save(node);
            systemLogService.log("Heartbeat received from " + nodeId, nodeId);
        }
    }

    public void detectFailures() {
        List<NodeStatus> nodes = nodeStatusRepository.findAll();
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(10);

        for (NodeStatus node : nodes) {
            if (node.getStatus().equals("ACTIVE") && node.getLastHeartbeat().isBefore(threshold)) {
                handleFailure(node);
            }
        }
    }

    public void simulateFailure(String nodeId) {
        Optional<NodeStatus> nodeOpt = nodeStatusRepository.findById(nodeId);
        if (nodeOpt.isPresent()) {
            handleFailure(nodeOpt.get());
        }
    }

    private void handleFailure(NodeStatus node) {
        node.setStatus("FAILED");
        nodeStatusRepository.save(node);

        System.out.println(node.getNodeId() + " heartbeat lost");
        systemLogService.log("Node failure detected: " + node.getNodeId(), "HEALTH_MONITOR");

        if (node.getRole().equals("LEADER")) {
            System.out.println("Leader failure! Triggering election...");
            // Simulate promotion
            triggerPromotion();
        } else {
            System.out.println("Replica promotion triggered");
            triggerPromotion();
        }

        recoveryManager.recoverSystem();
        System.out.println("Recovery completed");
        systemLogService.log("Recovery completed for " + node.getNodeId(), "SYSTEM");
    }

    private void triggerPromotion() {
        replicaManager.promoteReplica();
        // In a real system, we'd update the NodeStatus roles here
        List<NodeStatus> nodes = nodeStatusRepository.findAll();
        for (NodeStatus n : nodes) {
            if (n.getStatus().equals("ACTIVE") && n.getRole().equals("REPLICA")) {
                n.setRole("LEADER");
                nodeStatusRepository.save(n);
                systemLogService.log("Replica promoted to LEADER: " + n.getNodeId(), "SYSTEM");
                break;
            }
        }
    }
}
