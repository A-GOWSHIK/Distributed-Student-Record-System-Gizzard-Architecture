package com.student.records.services;

import com.student.records.domain.*;
import com.student.records.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class GradesService {

    private final GradesRepository gradesRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final SystemLogService systemLogService;

    public GradesService(GradesRepository gradesRepository,
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            SystemLogService systemLogService) {
        this.gradesRepository = gradesRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.systemLogService = systemLogService;
    }

    /**
     * Upsert: update existing grade for student+course, or create a new one.
     * Returns the saved record plus a flag indicating whether it was an update.
     */
    public java.util.Map<String, Object> assignGrade(Long studentId, Long courseId, Double marks) {
        Student student = studentRepository.findById(studentId).orElse(null);
        Course course = courseRepository.findById(courseId).orElse(null);

        if (student == null || course == null) return null;

        String grade = computeLetterGrade(marks);

        Optional<Grades> existing = gradesRepository.findByStudentIdAndCourseId(studentId, courseId);
        boolean isUpdate = existing.isPresent();

        Grades grades = isUpdate ? existing.get() : new Grades(student, course, marks, grade);
        if (isUpdate) {
            grades.setMarks(marks);
            grades.setGrade(grade);
        }

        Grades saved = gradesRepository.save(grades);

        String action = isUpdate ? "Grade UPDATED" : "Grade ASSIGNED";
        systemLogService.log(action + ": " + student.getName() + " got " + grade + " (" + marks + ") in "
                + course.getCourseName(), "Node1");

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("grade", saved);
        result.put("updated", isUpdate);
        result.put("message", isUpdate ? "Grade updated successfully" : "Grade assigned successfully");
        return result;
    }

    private String computeLetterGrade(Double marks) {
        if (marks >= 90) return "A+";
        if (marks >= 80) return "A";
        if (marks >= 70) return "B";
        if (marks >= 60) return "C";
        if (marks >= 50) return "D";
        return "F";
    }

    public List<Grades> getGradesByStudent(Long studentId) {
        return gradesRepository.findByStudentId(studentId);
    }

    /** For faculty: return all grades across all students */
    public List<Grades> getAllGrades() {
        return gradesRepository.findAll();
    }

    /** Update marks (and recalculate grade) for an existing grade record by ID */
    public java.util.Map<String, Object> updateGrade(Long gradeId, Double marks) {
        Optional<Grades> opt = gradesRepository.findById(gradeId);
        if (opt.isEmpty()) return null;

        Grades grades = opt.get();
        grades.setMarks(marks);
        grades.setGrade(computeLetterGrade(marks));
        Grades saved = gradesRepository.save(grades);

        systemLogService.log("Grade EDITED: id=" + gradeId + " new marks=" + marks
                + " grade=" + saved.getGrade(), "Node1");

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("grade", saved);
        result.put("message", "Grade updated successfully");
        return result;
    }

    /** Delete a grade record by ID */
    public boolean deleteGrade(Long gradeId) {
        if (!gradesRepository.existsById(gradeId)) return false;
        gradesRepository.deleteById(gradeId);
        systemLogService.log("Grade DELETED: id=" + gradeId, "Node1");
        return true;
    }
}
