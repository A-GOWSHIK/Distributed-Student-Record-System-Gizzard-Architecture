package com.student.records.services;

import java.util.List;

import com.student.records.domain.Student;

public interface StudentService {

    List<Student> getAllStudents();

    Student addStudent(Student student);

    java.util.Map<String, Object> getStudentDashboard(String email);
}
