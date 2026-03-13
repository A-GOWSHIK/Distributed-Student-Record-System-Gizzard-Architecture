package com.student.records.repository;

import com.student.records.domain.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStudentId(Long studentId);

    int countByStudentId(Long studentId);

    int countByStudentIdAndStatus(Long studentId, com.student.records.domain.AttendanceStatus status);
}
