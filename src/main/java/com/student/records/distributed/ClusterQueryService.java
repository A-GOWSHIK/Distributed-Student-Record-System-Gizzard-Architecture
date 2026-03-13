package com.student.records.distributed;

import com.student.records.domain.Student;
import com.student.records.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ClusterQueryService {

    @Autowired
    private StudentRepository studentRepository;

    private static final String NODE1 = "http://10.48.29.58:8081";
    private static final String NODE2 = "http://10.48.29.166:8082";
    private static final String NODE3 = "http://10.48.29.228:8083";

    @Value("${server.port}")
    private String serverPort;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Fetches students from ALL 3 nodes and deduplicates by student ID.
     * Each node calls the two OTHER nodes — never itself.
     */
    public List<Object> getAggregatedStudents() {
        Map<String, Object> uniqueStudents = new LinkedHashMap<>();

        // 1. Local students (always include self)
        List<Student> local = studentRepository.findAll();
        System.out.println("[CLUSTER QUERY] Local students on port " + serverPort + ": " + local.size());
        for (Student s : local) {
            uniqueStudents.put(String.valueOf(s.getId()), s);
        }

        // 2. Fetch from the two OTHER nodes
        List<String> remoteUrls = getRemoteNodeUrls();
        for (String remoteUrl : remoteUrls) {
            System.out.println("[CLUSTER QUERY] Fetching students from " + remoteUrl);
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer ADMIN");
                HttpEntity<?> entity = new HttpEntity<>(headers);

                ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                        remoteUrl + "/students", HttpMethod.GET, entity,
                        new ParameterizedTypeReference<List<Map<String, Object>>>() {});

                if (response.getBody() != null) {
                    System.out.println("[CLUSTER QUERY] Received " + response.getBody().size() + " students from " + remoteUrl);
                    for (Map<String, Object> s : response.getBody()) {
                        String idKey = String.valueOf(s.get("id"));
                        uniqueStudents.putIfAbsent(idKey, s); // deduplicate
                    }
                }
            } catch (Exception e) {
                System.out.println("[CLUSTER QUERY] " + remoteUrl + " unreachable: " + e.getMessage());
            }
        }

        List<Object> result = new ArrayList<>(uniqueStudents.values());
        System.out.println("[CLUSTER QUERY] Returning cluster student list: " + result.size() + " unique record(s)");
        return result;
    }

    /** Returns the URLs of the other two nodes (not this node). */
    private List<String> getRemoteNodeUrls() {
        List<String> all = Arrays.asList(NODE1, NODE2, NODE3);
        List<String> remotes = new ArrayList<>();
        for (String url : all) {
            if (!url.contains(serverPort)) remotes.add(url);
        }
        return remotes;
    }
}

