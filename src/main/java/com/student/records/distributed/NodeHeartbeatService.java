package com.student.records.distributed;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class NodeHeartbeatService {

    @Value("${node.id}")
    private String nodeId;

    @Scheduled(fixedRate = 5000)
    public void heartbeat() {
        System.out.println("[HEARTBEAT] Node is alive: " + nodeId);
    }
}
