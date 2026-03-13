package com.student.records.controller;

import com.student.records.domain.User;
import com.student.records.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * POST /auth/login
     * Body: { "username": "admin", "password": "admin123" }
     * Returns: { "token": "...", "role": "ADMIN", "username": "admin" }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Map<String, String> result = authService.login(username, password);
        if (result == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * GET /auth/profile
     * Header: Authorization: Bearer <token>
     * Returns the user profile for the authenticated user.
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing or invalid Authorization header"));
        }
        String token = authHeader.substring(7);
        User user = authService.getProfile(token);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole().name()));
    }
}
