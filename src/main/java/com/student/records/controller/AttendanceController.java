package com.student.records.controller;

import com.student.records.domain.AttendanceStatus;
import com.student.records.services.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    private String getRole(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return "";
    }

    /** POST /attendance — Faculty/Admin records attendance */
    @PostMapping
    public ResponseEntity<?> recordAttendance(@RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = getRole(authHeader);
        if (role.equals("STUDENT") || role.isEmpty())
            return ResponseEntity.status(403).build();

        Long studentId = Long.valueOf(body.get("studentId").toString());
        Long courseId = Long.valueOf(body.get("courseId").toString());
        LocalDate date = LocalDate.parse(body.get("date").toString());
        AttendanceStatus status = AttendanceStatus.valueOf(body.get("status").toString().toUpperCase());

        return ResponseEntity.ok(attendanceService.recordAttendance(studentId, courseId, date, status));
    }

    /** GET /attendance/all — Faculty: view all attendance records */
    @GetMapping("/all")
    public ResponseEntity<?> getAllAttendance(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = getRole(authHeader);
        if (role.equals("STUDENT") || role.isEmpty())
            return ResponseEntity.status(403).build();
        return ResponseEntity.ok(attendanceService.getAllAttendance());
    }

    /** GET /attendance/summary — Faculty: per-student-per-course attendance percentage */
    @GetMapping("/summary")
    public ResponseEntity<?> getAttendanceSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = getRole(authHeader);
        if (role.equals("STUDENT") || role.isEmpty())
            return ResponseEntity.status(403).build();
        return ResponseEntity.ok(attendanceService.getAttendanceSummary());
    }

    /** GET /attendance/student/{id} — View attendance for a specific student */
    @GetMapping("/student/{id}")
    public ResponseEntity<?> getAttendanceByStudent(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (getRole(authHeader).isEmpty())
            return ResponseEntity.status(403).build();
        return ResponseEntity.ok(attendanceService.getAttendanceByStudent(id));
    }
}
