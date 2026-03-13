package com.student.records.controller;

import com.student.records.domain.Course;
import com.student.records.services.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    @Autowired
    private CourseService courseService;

    private String getRole(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return "";
    }

    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody Course course,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!getRole(authHeader).equals("ADMIN"))
            return ResponseEntity.status(403).build();
        return ResponseEntity.ok(courseService.createCourse(course));
    }

    @GetMapping
    public ResponseEntity<?> getAllCourses(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = getRole(authHeader);
        if (role.equals("STUDENT") || role.isEmpty())
            return ResponseEntity.status(403).build();
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = getRole(authHeader);
        if (role.equals("STUDENT") || role.isEmpty())
            return ResponseEntity.status(403).build();
        return courseService.getCourseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
