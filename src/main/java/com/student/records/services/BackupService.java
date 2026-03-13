package com.student.records.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BackupService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Queries the student_backup table directly to show replicated records.
     */
    public List<Map<String, Object>> getBackupStudents() {
        return jdbcTemplate.queryForList("SELECT * FROM student_backup");
    }
}
