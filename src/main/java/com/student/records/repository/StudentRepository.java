package com.student.records.repository;

import com.student.records.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Feature 6: Search by name (case-insensitive, partial match)
    List<Student> findByNameContainingIgnoreCase(String name);

    // Feature 6: Search by course (case-insensitive, partial match)
    List<Student> findByCourseContainingIgnoreCase(String course);

    // Get student by email
    java.util.Optional<Student> findByEmail(String email);
}
