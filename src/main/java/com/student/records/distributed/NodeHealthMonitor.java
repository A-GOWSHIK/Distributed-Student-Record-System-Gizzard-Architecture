package com.student.records.distributed;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NodeHealthMonitor {

    @Value("${server.port}")
    private String serverPort;

    private static final String NODE1_PING = "http://10.48.29.58:8081/replica/ping";
    private static final String NODE2_PING = "http://10.48.29.166:8082/replica/ping";
    private static final String NODE3_PING = "http://10.48.29.228:8083/replica/ping";

    private final RestTemplate restTemplate = new RestTemplate();

    private volatile long lastNode1OkMs = System.currentTimeMillis();
    private volatile boolean node1Up = true;

    private volatile long lastNode2OkMs = System.currentTimeMillis();
    private volatile boolean node2Up = true;

    private volatile long lastNode3OkMs = System.currentTimeMillis();
    private volatile boolean node3Up = true;

    @Scheduled(fixedRate = 5000)
    public void checkHealth() {
        // --- Node1 ---
        try {
            if ("8081".equals(serverPort)) { node1Up = true; lastNode1OkMs = System.currentTimeMillis(); }
            else {
                restTemplate.getForObject(NODE1_PING, String.class);
                lastNode1OkMs = System.currentTimeMillis();
                if (!node1Up) { System.out.println("[FAILOVER] Node1 back online"); node1Up = true; }
            }
        } catch (Exception e) {
            if (System.currentTimeMillis() - lastNode1OkMs > 10000 && node1Up) {
                System.out.println("[FAILOVER] Node1 unreachable");
                node1Up = false;
            }
        }

        // --- Node2 ---
        try {
            if ("8082".equals(serverPort)) { node2Up = true; lastNode2OkMs = System.currentTimeMillis(); }
            else {
                restTemplate.getForObject(NODE2_PING, String.class);
                lastNode2OkMs = System.currentTimeMillis();
                if (!node2Up) { System.out.println("[FAILOVER] Node2 back online"); node2Up = true; }
            }
        } catch (Exception e) {
            if (System.currentTimeMillis() - lastNode2OkMs > 10000 && node2Up) {
                System.out.println("[FAILOVER] Node2 unreachable");
                node2Up = false;
            }
        }

        // --- Node3 ---
        try {
            if ("8083".equals(serverPort)) { node3Up = true; lastNode3OkMs = System.currentTimeMillis(); }
            else {
                restTemplate.getForObject(NODE3_PING, String.class);
                lastNode3OkMs = System.currentTimeMillis();
                if (!node3Up) { System.out.println("[FAILOVER] Node3 back online"); node3Up = true; }
            }
        } catch (Exception e) {
            if (System.currentTimeMillis() - lastNode3OkMs > 10000 && node3Up) {
                System.out.println("[FAILOVER] Node3 unreachable");
                node3Up = false;
            }
        }
    }

    public boolean isNode1Up() { return node1Up; }
    public boolean isNode2Up() { return node2Up; }
    public boolean isNode3Up() { return node3Up; }
}
