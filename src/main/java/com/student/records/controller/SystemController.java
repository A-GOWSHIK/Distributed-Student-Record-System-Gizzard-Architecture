package com.student.records.controller;

import com.student.records.services.BackupService;
import com.student.records.services.ClusterService;
import com.student.records.services.MetricsService;
import com.student.records.services.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class SystemController {

    @Autowired
    private SystemLogService systemLogService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private BackupService backupService;

    private String getRole(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer "))
            return authHeader.substring(7);
        return "";
    }

    @GetMapping("/system/logs")
    public ResponseEntity<?> getSystemLogs(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!getRole(authHeader).equals("ADMIN"))
            return ResponseEntity.status(403).build();
        return ResponseEntity.ok(systemLogService.getAllLogs());
    }

    @GetMapping("/cluster/status")
    public ResponseEntity<?> getClusterStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = getRole(authHeader);
        if (role.equals("STUDENT") || role.isEmpty())
            return ResponseEntity.status(403).build();
        return ResponseEntity.ok(clusterService.getClusterStatus());
    }

    @GetMapping("/cluster/metrics")
    public ResponseEntity<?> getClusterMetrics(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = getRole(authHeader);
        if (role.equals("STUDENT") || role.isEmpty())
            return ResponseEntity.status(403).build();
        return ResponseEntity.ok(metricsService.getMetrics());
    }

    @GetMapping("/backup/students")
    public ResponseEntity<?> getBackupStudents(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!getRole(authHeader).equals("ADMIN"))
            return ResponseEntity.status(403).build();
        return ResponseEntity.ok(backupService.getBackupStudents());
    }

    @GetMapping("/monitor")
    public ResponseEntity<?> getMonitorDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = getRole(authHeader);
        if (role.equals("STUDENT") || role.isEmpty())
            return ResponseEntity.status(403).build();
        Map<String, Object> status = clusterService.getClusterStatus();
        status.put("lastFailureEvent", "None");
        status.put("replicaNodes", status.get("replicas"));
        return ResponseEntity.ok(status);
    }
}
