package com.student.records.edge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EdgeStudentService {

    @Autowired
    private EdgeCacheManager cacheManager;

    public String getStudentData(Long id) {

        if (cacheManager.isStudentCached(id)) {

            System.out.println("Fetching student from EDGE CACHE");

            return cacheManager.getStudentFromCache(id);
        }

        System.out.println("Fetching student from CENTRAL SERVICE");

        String studentName = "Student-" + id;

        cacheManager.cacheStudent(id, studentName);

        return studentName;
    }
}
