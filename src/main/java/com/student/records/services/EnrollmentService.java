package com.student.records.services;

import com.student.records.domain.*;
import com.student.records.repository.*;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final SystemLogService systemLogService;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            SystemLogService systemLogService,
            org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.systemLogService = systemLogService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void cleanupDuplicateEnrollments() {
        try {
            int deleted = jdbcTemplate.update(
                    "DELETE FROM enrollment e USING enrollment e2 WHERE e.id > e2.id AND e.student_id = e2.student_id AND e.course_id = e2.course_id");
            if (deleted > 0) {
                systemLogService.log("Database Cleanup: Removed " + deleted + " duplicate enrollment rows.", "Node1");
            }
        } catch (Exception e) {
            System.err.println("Could not cleanup duplicates: " + e.getMessage());
        }
    }

    public Enrollment requestEnrollment(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId).orElse(null);
        Course course = courseRepository.findById(courseId).orElse(null);

        if (student == null || course == null)
            return null;

        List<Enrollment> existing = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);
        boolean hasActive = existing.stream()
                .anyMatch(e -> e.getStatus() == EnrollmentStatus.PENDING || e.getStatus() == EnrollmentStatus.APPROVED);

        if (hasActive) {
            return null; // Signals duplicate
        }

        Enrollment enrollment = new Enrollment(student, course, EnrollmentStatus.PENDING);
        Enrollment saved = enrollmentRepository.save(enrollment);

        systemLogService.log("Enrollment PENDING: Student=" + student.getName() + ", Course=" + course.getCourseName(),
                "Node1");
        return saved;
    }

    public Enrollment approveEnrollment(Long enrollmentId, boolean approve) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElse(null);
        if (enrollment != null) {
            enrollment.setStatus(approve ? EnrollmentStatus.APPROVED : EnrollmentStatus.REJECTED);
            Enrollment saved = enrollmentRepository.save(enrollment);
            systemLogService.log("Enrollment " + saved.getStatus() + ": ID=" + enrollmentId, "Node1");
            return saved;
        }
        return null;
    }

    public List<Enrollment> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    public List<Enrollment> getEnrollmentsByStatus(EnrollmentStatus status) {
        return enrollmentRepository.findByStatus(status);
    }

    public List<Enrollment> getEnrollmentsByStudentAndStatus(Student student, EnrollmentStatus status) {
        return enrollmentRepository.findByStudentAndStatus(student, status);
    }

    public Enrollment enrollStudent(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId).orElse(null);
        Course course = courseRepository.findById(courseId).orElse(null);

        if (student == null || course == null)
            return null;

        Enrollment enrollment = new Enrollment(student, course, EnrollmentStatus.APPROVED);
        Enrollment saved = enrollmentRepository.save(enrollment);

        systemLogService.log(
                "Enrollment APPROVED: Admin enrolled Student=" + student.getName() + " into Course="
                        + course.getCourseName(),
                "Node1");
        return saved;
    }
}
