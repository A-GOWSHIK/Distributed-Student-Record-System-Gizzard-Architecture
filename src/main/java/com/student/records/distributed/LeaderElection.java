package com.student.records.distributed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Bully Algorithm Leader Election.
 *
 * Node IDs: node1=1, node2=2, node3=3
 * Rule: highest active node ID is leader.
 *
 * On startup and every 30 seconds, each node determines
 * the highest-ID active node by pinging all higher-ID nodes.
 * If no higher node is alive, this node declares itself leader.
 */
@Service
public class LeaderElection {

    @Value("${node.id}")
    private String nodeId;

    @Value("${server.port}")
    private String serverPort;

    @Autowired
    private NodeHealthMonitor nodeHealthMonitor;

    private static final String NODE1 = "http://10.48.29.58:8081";
    private static final String NODE2 = "http://10.48.29.166:8082";
    private static final String NODE3 = "http://10.48.29.228:8083";

    private volatile String currentLeader = "node1"; // default Node1 as initial leader

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Bully Election — runs every 30 seconds.
     * Highest active nodeId wins.
     */
    @Scheduled(fixedRate = 30000, initialDelay = 5000)
    public void runElection() {
        int myId = getNodePriority(nodeId);
        System.out.println("[LeaderElection] Running Bully Election. I am " + nodeId + " (priority=" + myId + ")");

        // Check if any higher-priority node is alive
        String highestAliveLeader = nodeId;
        int highestPriority = myId;

        if (isAlive(NODE3) && getNodePriority("node3") > highestPriority) {
            highestAliveLeader = "node3";
            highestPriority = getNodePriority("node3");
        }
        if (isAlive(NODE2) && getNodePriority("node2") > highestPriority) {
            highestAliveLeader = "node2";
            highestPriority = getNodePriority("node2");
        }
        if (isAlive(NODE1) && getNodePriority("node1") > highestPriority) {
            highestAliveLeader = "node1";
        }

        if (!highestAliveLeader.equals(currentLeader)) {
            System.out.println("[LeaderElection] Leader changed: " + currentLeader + " → " + highestAliveLeader);
            currentLeader = highestAliveLeader;
        } else {
            System.out.println("[LeaderElection] Leader confirmed: " + currentLeader);
        }
    }

    private boolean isAlive(String baseUrl) {
        if (baseUrl.contains(serverPort)) return true; // prevent self-ping
        try {
            restTemplate.getForObject(baseUrl + "/replica/ping", String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private int getNodePriority(String id) {
        switch (id) {
            case "node3": return 3;
            case "node2": return 2;
            default:      return 1;
        }
    }

    public String getCurrentLeader() { return currentLeader; }
    public boolean isLeader() { return nodeId.equals(currentLeader); }

    /** Compatibility method — same signature as old distributedcore version. */
    public String electLeader(String[] nodes) {
        runElection();
        return currentLeader;
    }

    /** Alias for getCurrentLeader() — used by ClusterService. */
    public String getLeader() { return currentLeader; }
}
