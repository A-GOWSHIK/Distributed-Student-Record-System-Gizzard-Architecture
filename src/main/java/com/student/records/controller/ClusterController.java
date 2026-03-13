package com.student.records.controller;

import com.student.records.distributed.ClusterQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes distributed cluster-level endpoints.
 * GET /cluster/students → aggregates students from BOTH Node1 and Node2.
 */
@RestController
@CrossOrigin(origins = "*")
public class ClusterController {

    @Autowired
    private ClusterQueryService clusterQueryService;

    @GetMapping("/cluster/students")
    public ResponseEntity<List<Object>> getClusterStudents(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        System.out.println("[AGGREGATION] /cluster/students requested — gathering from all nodes");
        List<Object> all = clusterQueryService.getAggregatedStudents();
        return ResponseEntity.ok(all);
    }
}
