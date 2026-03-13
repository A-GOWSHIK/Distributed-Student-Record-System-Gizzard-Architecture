package com.student.records.controller;

import com.student.records.services.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/enrollment")
@CrossOrigin(origins = "*")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private com.student.records.repository.StudentRepository studentRepository;

    private String getRole(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return "";
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestEnrollment(@RequestBody Map<String, Long> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = getRole(authHeader);
        if (role.equals("FACULTY") || role.isEmpty())
            return ResponseEntity.status(403).build();
        com.student.records.domain.Enrollment enrollment = enrollmentService.requestEnrollment(body.get("studentId"),
                body.get("courseId"));
        if (enrollment == null) {
            return ResponseEntity.status(400).body("Already requested or already enrolled.");
        }
        return ResponseEntity.ok(enrollment);
    }

    @PostMapping("/admin-enroll")
    public ResponseEntity<?> adminEnrollStudent(@RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = getRole(authHeader);
        if (!role.equals("ADMIN"))
            return ResponseEntity.status(403).build();

        Long studentId = Long.valueOf(body.get("studentId").toString());
        Long courseId = Long.valueOf(body.get("courseId").toString());

        return ResponseEntity.ok(enrollmentService.enrollStudent(studentId, courseId));
    }

    @PostMapping("/approve")
    public ResponseEntity<?> approveEnrollment(@RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = getRole(authHeader);
        if (role.equals("STUDENT") || role.isEmpty())
            return ResponseEntity.status(403).build();

        Long id = Long.valueOf(body.get("enrollmentId").toString());
        boolean approve = (boolean) body.get("approve");
        return ResponseEntity.ok(enrollmentService.approveEnrollment(id, approve));
    }

    @GetMapping("/student/{id}")
    public ResponseEntity<?> getEnrollmentsByStudent(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (getRole(authHeader).isEmpty())
            return ResponseEntity.status(403).build();
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudent(id));
    }

    @PostMapping("/student-request")
    public ResponseEntity<?> studentRequestEnrollment(@RequestBody Map<String, Long> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-User-Email", required = false) String email) {
        String role = getRole(authHeader);
        if (!role.equals("STUDENT"))
            return ResponseEntity.status(403).build();

        com.student.records.domain.Student student = studentRepository.findByEmail(email).orElse(null);
        if (student == null)
            return ResponseEntity.status(404).build();

        com.student.records.domain.Enrollment enrollment = enrollmentService.requestEnrollment(student.getId(),
                body.get("courseId"));
        if (enrollment == null) {
            return ResponseEntity.status(400).body("Already requested or already enrolled.");
        }
        return ResponseEntity.ok(enrollment);
    }

    @GetMapping("/admin-requests")
    public ResponseEntity<?> getAdminEnrollmentRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!getRole(authHeader).equals("ADMIN"))
            return ResponseEntity.status(403).build();
        return ResponseEntity
                .ok(enrollmentService.getEnrollmentsByStatus(com.student.records.domain.EnrollmentStatus.PENDING));
    }

    @PostMapping("/admin-approve")
    public ResponseEntity<?> adminApproveEnrollment(@RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!getRole(authHeader).equals("ADMIN"))
            return ResponseEntity.status(403).build();

        Long id = Long.valueOf(body.get("enrollmentId").toString());
        return ResponseEntity.ok(enrollmentService.approveEnrollment(id, true));
    }

    @PostMapping("/admin-reject")
    public ResponseEntity<?> adminRejectEnrollment(@RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!getRole(authHeader).equals("ADMIN"))
            return ResponseEntity.status(403).build();

        Long id = Long.valueOf(body.get("enrollmentId").toString());
        return ResponseEntity.ok(enrollmentService.approveEnrollment(id, false));
    }
}
