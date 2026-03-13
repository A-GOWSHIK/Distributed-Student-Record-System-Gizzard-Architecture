package com.student.records.repository;

import com.student.records.domain.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

        List<Enrollment> findByStudentId(Long studentId);

        int countByStudent(com.student.records.domain.Student student);

        int countByStudentAndStatus(com.student.records.domain.Student student,
                        com.student.records.domain.EnrollmentStatus status);

        List<Enrollment> findByStudentAndStatus(com.student.records.domain.Student student,
                        com.student.records.domain.EnrollmentStatus status);

        List<Enrollment> findByStatus(com.student.records.domain.EnrollmentStatus status);

        List<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
}
