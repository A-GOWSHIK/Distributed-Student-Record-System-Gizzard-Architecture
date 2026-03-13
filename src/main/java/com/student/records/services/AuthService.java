package com.student.records.services;

import com.student.records.domain.User;
import com.student.records.domain.UserRole;
import com.student.records.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    // In-memory token store: token -> username
    private final Map<String, String> tokenStore = new HashMap<>();

    /**
     * Seed default users once on startup if none exist.
     */
    @PostConstruct
    public void seedDefaultUsers() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            userRepository.save(new User("admin", "admin123", UserRole.ADMIN));
        }
        if (userRepository.findByUsername("faculty").isEmpty()) {
            userRepository.save(new User("faculty", "faculty123", UserRole.FACULTY));
        }
        if (userRepository.findByUsername("student").isEmpty()) {
            userRepository.save(new User("student", "student123", UserRole.STUDENT));
        }
        System.out.println("[AuthService] Default users seeded or verified: admin / faculty / student");
    }

    /**
     * Authenticate user. Returns a map with token and role, or null on failure.
     */
    public Map<String, String> login(String username, String password) {
        Optional<User> opt = userRepository.findByUsernameAndPassword(username, password);
        if (opt.isEmpty()) {
            return null;
        }
        User user = opt.get();
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, username);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", user.getRole().name());
        response.put("username", user.getUsername());
        return response;
    }

    /**
     * Get user profile by Authorization token header value.
     */
    public User getProfile(String token) {
        String username = tokenStore.get(token);
        if (username == null)
            return null;
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Validate a bearer token — used by other services for lightweight auth checks.
     */
    public boolean isValidToken(String token) {
        return tokenStore.containsKey(token);
    }
}
