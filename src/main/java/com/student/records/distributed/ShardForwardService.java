package com.student.records.distributed;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ShardForwardService {

    private final RestTemplate restTemplate = new RestTemplate();

    private HttpHeaders adminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer ADMIN");
        return headers;
    }

    /** Forward POST (create student) */
    public Object forward(String url, Object body) {
        HttpEntity<Object> request = new HttpEntity<>(body, adminHeaders());
        return restTemplate.postForObject(url, request, Object.class);
    }

    /** Forward PUT (update student) */
    public Object forwardPut(String url, Object body) {
        HttpEntity<Object> request = new HttpEntity<>(body, adminHeaders());
        ResponseEntity<Object> response = restTemplate.exchange(
                url, HttpMethod.PUT, request, Object.class);
        return response.getBody();
    }

    /** Forward DELETE (delete student) */
    public void forwardDelete(String url) {
        HttpEntity<?> request = new HttpEntity<>(adminHeaders());
        restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
    }
}
