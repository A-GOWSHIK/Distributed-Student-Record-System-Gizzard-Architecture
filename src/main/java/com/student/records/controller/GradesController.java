package com.student.records.controller;

import com.student.records.services.GradesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/grades")
@CrossOrigin(origins = "*")
public class GradesController {

    @Autowired
    private GradesService gradesService;

    private String getRole(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return "";
    }

    /** POST /grades — Faculty/Admin assigns or updates a grade (upsert) */
    @PostMapping
    public ResponseEntity<?> assignGrade(@RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = getRole(authHeader);
        if (role.equals("STUDENT") || role.isEmpty())
            return ResponseEntity.status(403).build();

        Long studentId = Long.valueOf(body.get("studentId").toString());
        Long courseId = Long.valueOf(body.get("courseId").toString());
        Double marks = Double.valueOf(body.get("marks").toString());

        Map<String, Object> result = gradesService.assignGrade(studentId, courseId, marks);
        if (result == null)
            return ResponseEntity.badRequest().body("Student or course not found");
        return ResponseEntity.ok(result);
    }

    /** GET /grades/all — Faculty: view all grades for all students */
    @GetMapping("/all")
    public ResponseEntity<?> getAllGrades(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = getRole(authHeader);
        if (role.equals("STUDENT") || role.isEmpty())
            return ResponseEntity.status(403).build();
        return ResponseEntity.ok(gradesService.getAllGrades());
    }

    /** PUT /grades/{id} — Faculty: update marks and recalculate grade */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGrade(@PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = getRole(authHeader);
        if (role.equals("STUDENT") || role.isEmpty())
            return ResponseEntity.status(403).build();

        Double marks = Double.valueOf(body.get("marks").toString());
        Map<String, Object> result = gradesService.updateGrade(id, marks);
        if (result == null)
            return ResponseEntity.status(404).body("Grade record not found");
        return ResponseEntity.ok(result);
    }

    /** DELETE /grades/{id} — Faculty: remove a grade record */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGrade(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = getRole(authHeader);
        if (role.equals("STUDENT") || role.isEmpty())
            return ResponseEntity.status(403).build();

        boolean deleted = gradesService.deleteGrade(id);
        if (!deleted)
            return ResponseEntity.status(404).body("Grade record not found");
        return ResponseEntity.ok(Map.of("message", "Grade deleted successfully"));
    }
}
