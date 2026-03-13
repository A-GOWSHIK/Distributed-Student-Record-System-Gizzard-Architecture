package com.student.records;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StudentRecordSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentRecordSystemApplication.class, args);
    }

}
