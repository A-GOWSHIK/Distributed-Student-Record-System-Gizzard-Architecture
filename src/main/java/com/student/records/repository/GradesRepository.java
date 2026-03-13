package com.student.records.repository;

import com.student.records.domain.Grades;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradesRepository extends JpaRepository<Grades, Long> {

    List<Grades> findByStudentId(Long studentId);

    java.util.Optional<Grades> findByStudentIdAndCourseId(Long studentId, Long courseId);
}
