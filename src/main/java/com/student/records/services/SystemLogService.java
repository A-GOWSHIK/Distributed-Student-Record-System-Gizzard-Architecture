package com.student.records.services;

import com.student.records.domain.SystemLog;
import com.student.records.repository.SystemLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SystemLogService {

    @Autowired
    private SystemLogRepository systemLogRepository;

    /**
     * Record a system event with a node name and current timestamp.
     */
    public SystemLog log(String event, String node) {
        SystemLog logEntry = new SystemLog(event, node, LocalDateTime.now());
        System.out.println("[SystemLog] [" + node + "] " + event);
        return systemLogRepository.save(logEntry);
    }

    /**
     * Get all system events ordered by id descending.
     */
    public List<SystemLog> getAllLogs() {
        return systemLogRepository.findAll();
    }
}
