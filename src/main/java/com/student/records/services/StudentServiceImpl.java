package com.student.records.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.student.records.domain.Student;
import com.student.records.domain.User;
import com.student.records.domain.UserRole;
import com.student.records.repository.StudentRepository;
import com.student.records.repository.UserRepository;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.student.records.repository.EnrollmentRepository enrollmentRepository;

    @Autowired
    private com.student.records.repository.AttendanceRepository attendanceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Student> getAllStudents() {
        return repository.findAll();
    }

    @Override
    public Student addStudent(Student student) {

        Student savedStudent = repository.save(student);

        User user = new User();
        user.setUsername(student.getEmail());
        user.setPassword(student.getPassword());
        user.setRole(UserRole.STUDENT);
        userRepository.save(user);

        try {
            // Auto-create backup table if it doesn't exist (e.g. on Node2)
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS student_backup " +
                "(id BIGINT PRIMARY KEY, name VARCHAR(255), email VARCHAR(255), course VARCHAR(255))"
            );
            jdbcTemplate.update(
                    "INSERT INTO student_backup(id, name, email, course) VALUES (?, ?, ?, ?)",
                    savedStudent.getId(),
                    savedStudent.getName(),
                    savedStudent.getEmail(),
                    savedStudent.getCourse());
            System.out.println("[BACKUP] Replication to student_backup successful for id=" + savedStudent.getId());
        } catch (Exception e) {
            System.out.println("[BACKUP] Warning: backup table insert failed (non-critical): " + e.getMessage());
        }

        return savedStudent;
    }

    @Override
    public java.util.Map<String, Object> getStudentDashboard(String email) {
        Student student = repository.findByEmail(email).orElse(null);
        if (student == null)
            return null;

        int totalCourses = enrollmentRepository.countByStudentAndStatus(student,
                com.student.records.domain.EnrollmentStatus.APPROVED);

        // If student has no enrolled courses, attendance is 0% — not 100%
        int attendancePercentage = 0;
        if (totalCourses > 0) {
            int totalClasses = attendanceRepository.countByStudentId(student.getId());
            int presentClasses = attendanceRepository.countByStudentIdAndStatus(
                    student.getId(), com.student.records.domain.AttendanceStatus.PRESENT);
            attendancePercentage = totalClasses == 0 ? 0 : (presentClasses * 100) / totalClasses;
        }

        List<String> quotes = List.of(
                "Learning never exhausts the mind.",
                "Success is built on discipline.",
                "Small progress is still progress.",
                "Consistency beats talent.",
                "Education is the passport to the future.");
        String quote = quotes.get(new java.util.Random().nextInt(quotes.size()));

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("name", student.getName());
        response.put("totalCourses", totalCourses);
        response.put("attendancePercentage", attendancePercentage);
        response.put("noCoursesEnrolled", totalCourses == 0);
        response.put("quote", quote);

        return response;
    }
}
