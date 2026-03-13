package com.student.records.distributed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Centralized RPC Client — all inter-node HTTP calls go through here.
 * Automatically attaches:
 *   - Authorization: Bearer ADMIN
 *   - X-Lamport-Timestamp: <current clock value>
 * And updates the Lamport clock from the response header on receipt.
 */
@Service
public class RpcClient {

    @Autowired
    private LogicalClock logicalClock;

    private final RestTemplate restTemplate = new RestTemplate();

    public static final String LAMPORT_HEADER = "X-Lamport-Timestamp";

    /**
     * HTTP GET with Lamport timestamp attached.
     */
    public ResponseEntity<String> get(String url) {
        HttpHeaders headers = buildHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        syncClockFromResponse(response);
        System.out.println("[RPC] GET " + url + " [Lamport=" + logicalClock.getTime() + "]");
        return response;
    }

    /**
     * HTTP POST with Lamport timestamp attached.
     */
    public ResponseEntity<String> post(String url, Object body) {
        HttpHeaders headers = buildHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        syncClockFromResponse(response);
        System.out.println("[RPC] POST " + url + " [Lamport=" + logicalClock.getTime() + "]");
        return response;
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer ADMIN");
        headers.set(LAMPORT_HEADER, String.valueOf(logicalClock.tick())); // tick before send
        return headers;
    }

    private void syncClockFromResponse(ResponseEntity<?> response) {
        String remoteTs = response.getHeaders().getFirst(LAMPORT_HEADER);
        if (remoteTs != null) {
            try {
                logicalClock.update(Long.parseLong(remoteTs));
            } catch (NumberFormatException ignored) {}
        }
    }
}
