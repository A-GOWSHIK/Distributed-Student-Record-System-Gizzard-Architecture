package com.student.records.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "node_status")
public class NodeStatus {

    @Id
    private String nodeId;

    private String status;
    private LocalDateTime lastHeartbeat;
    private String role;

    public NodeStatus() {
    }

    public NodeStatus(String nodeId, String status, LocalDateTime lastHeartbeat, String role) {
        this.nodeId = nodeId;
        this.status = status;
        this.lastHeartbeat = lastHeartbeat;
        this.role = role;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
