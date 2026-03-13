package com.student.records.edge;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class EdgeCacheManager {

    private Map<Long, String> studentCache = new HashMap<>();

    public void cacheStudent(Long id, String name) {
        studentCache.put(id, name);
        System.out.println("Student cached at edge node: " + name);
    }

    public String getStudentFromCache(Long id) {
        return studentCache.get(id);
    }

    public boolean isStudentCached(Long id) {
        return studentCache.containsKey(id);
    }
}
