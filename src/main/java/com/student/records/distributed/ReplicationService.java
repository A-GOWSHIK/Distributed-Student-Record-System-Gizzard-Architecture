package com.student.records.distributed;

import com.student.records.domain.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ReplicationService {

    @Autowired
    private ShardRouter shardRouter;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${server.port}")
    private String serverPort;

    /**
     * Ring replication: replicate the saved student to the next node.
     *   shard0 (Node1) → replica on Node2
     *   shard1 (Node2) → replica on Node3
     *   shard2 (Node3) → replica on Node1
     */
    public void replicate(Student student) {
        if (student.getId() == null) return;

        String replicaBase = shardRouter.getReplicaBase(student.getId().intValue());
        String replicaUrl  = replicaBase + "/replica/students";

        System.out.println("[REPLICATION] Replicating student id=" + student.getId() + " → " + replicaBase);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer ADMIN");
            HttpEntity<Student> request = new HttpEntity<>(student, headers);

            restTemplate.postForObject(replicaUrl, request, Object.class);
            System.out.println("[REPLICATION] Success — student id=" + student.getId() + " stored on replica");
        } catch (Exception e) {
            System.out.println("[REPLICATION] Failed to replicate student id=" + student.getId()
                    + ": " + e.getMessage());
        }
    }
}

