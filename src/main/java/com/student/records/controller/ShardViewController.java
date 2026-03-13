package com.student.records.controller;

import com.student.records.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Shard inspection endpoints — admin only (enforced by frontend RBAC).
 * GET /shard/node1 → Node1 local data
 * GET /shard/node2 → proxied from Node2
 * GET /shard/node3 → proxied from Node3
 */
@RestController
@CrossOrigin(origins = "*")
public class ShardViewController {

    @Autowired
    private StudentRepository studentRepository;

    private static final String NODE2_STUDENTS = "http://10.48.29.166:8082/students";
    private static final String NODE3_STUDENTS = "http://10.48.29.228:8083/students";
    private final RestTemplate restTemplate = new RestTemplate();

    /** Node1 local shard data (id%3==0) */
    @GetMapping("/shard/node1")
    public ResponseEntity<?> getNode1Shard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        System.out.println("[SHARD VIEW] Showing Node1 shard data");
        return ResponseEntity.ok(studentRepository.findAll());
    }

    /** Proxy to Node2 shard data (id%3==1) */
    @GetMapping("/shard/node2")
    public ResponseEntity<?> getNode2Shard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        System.out.println("[SHARD VIEW] Fetching Node2 shard data");
        return ResponseEntity.ok(proxyGet(NODE2_STUDENTS));
    }

    /** Proxy to Node3 shard data (id%3==2) */
    @GetMapping("/shard/node3")
    public ResponseEntity<?> getNode3Shard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        System.out.println("[SHARD VIEW] Fetching Node3 shard data");
        return ResponseEntity.ok(proxyGet(NODE3_STUDENTS));
    }

    private List<Object> proxyGet(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer ADMIN");
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<List<Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<List<Object>>() {});
            return response.getBody() != null ? response.getBody() : List.of();
        } catch (Exception e) {
            System.out.println("[SHARD VIEW] Unreachable: " + url + " — " + e.getMessage());
            return List.of();
        }
    }
}

