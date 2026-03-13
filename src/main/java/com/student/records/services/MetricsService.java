package com.student.records.services;

import com.student.records.repository.CourseRepository;
import com.student.records.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MetricsService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ClusterService clusterService;

    /**
     * Returns aggregated statistics about the distributed student system.
     */
    public Map<String, Object> getMetrics() {
        long totalStudents = studentRepository.count();
        long totalCourses = courseRepository.count();
        int activeNodes = clusterService.getActiveNodeCount();

        String systemLoad;
        if (totalStudents < 50)
            systemLoad = "LOW";
        else if (totalStudents < 200)
            systemLoad = "MEDIUM";
        else
            systemLoad = "HIGH";

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalStudents", totalStudents);
        metrics.put("totalCourses", totalCourses);
        metrics.put("activeNodes", activeNodes);
        metrics.put("replicas", 2);
        metrics.put("systemLoad", systemLoad);
        return metrics;
    }
}
