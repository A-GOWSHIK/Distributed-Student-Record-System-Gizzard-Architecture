package com.student.records.services;

import com.student.records.domain.*;
import com.student.records.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final SystemLogService systemLogService;

    public AttendanceService(AttendanceRepository attendanceRepository,
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            SystemLogService systemLogService) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.systemLogService = systemLogService;
    }

    public Attendance recordAttendance(Long studentId, Long courseId, LocalDate date, AttendanceStatus status) {
        Student student = studentRepository.findById(studentId).orElse(null);
        Course course = courseRepository.findById(courseId).orElse(null);

        if (student == null || course == null)
            return null;

        Attendance attendance = new Attendance(student, course, date, status);
        Attendance saved = attendanceRepository.save(attendance);

        systemLogService.log(
                "Attendance recorded: " + student.getName() + " is " + status + " for " + course.getCourseName(),
                "Node1");
        return saved;
    }

    public List<Attendance> getAttendanceByStudent(Long studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }

    /** For faculty: return all attendance records across all students */
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    /**
     * Compute per-student-per-course attendance percentage summary.
     * Returns a list of maps: { studentName, courseName, present, total, percentage }
     */
    public List<java.util.Map<String, Object>> getAttendanceSummary() {
        List<Attendance> all = attendanceRepository.findAll();

        // Group by student+course
        java.util.Map<String, java.util.Map<String, Object>> grouped = new java.util.LinkedHashMap<>();
        for (Attendance a : all) {
            String key = a.getStudent().getId() + "-" + a.getCourse().getId();
            grouped.putIfAbsent(key, new java.util.HashMap<>(java.util.Map.of(
                    "studentName", a.getStudent().getName(),
                    "courseName",  a.getCourse().getCourseName(),
                    "present",     0,
                    "total",       0
            )));
            java.util.Map<String, Object> entry = grouped.get(key);
            entry.put("total", (int) entry.get("total") + 1);
            if (a.getStatus() == AttendanceStatus.PRESENT) {
                entry.put("present", (int) entry.get("present") + 1);
            }
        }

        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (java.util.Map<String, Object> entry : grouped.values()) {
            int total   = (int) entry.get("total");
            int present = (int) entry.get("present");
            entry.put("percentage", total == 0 ? 0 : (present * 100) / total);
            result.add(entry);
        }
        return result;
    }
}
