package com.student.records.controller;

import com.student.records.services.NodeFailureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/simulate")
@CrossOrigin(origins = "*")
public class NodeSimulationController {

    @Autowired
    private NodeFailureService nodeFailureService;

    /**
     * POST /simulate/node-failure
     * Body (optional): { "nodeName": "Node2" }
     * Simulates a node failure, promotes replica, and triggers recovery.
     *
     * Console output:
     * [NodeFailure] Node2 failed
     * [NodeFailure] Promoting replica for Node2
     * System recovery initiated using last checkpoint
     * [NodeFailure] System recovered after Node2 failure
     */
    @PostMapping("/node-failure")
    public Map<String, String> simulateNodeFailure(@RequestBody(required = false) Map<String, String> body) {
        String nodeName = (body != null && body.containsKey("nodeName")) ? body.get("nodeName") : "Node2";
        return nodeFailureService.simulateNodeFailure(nodeName);
    }
}
