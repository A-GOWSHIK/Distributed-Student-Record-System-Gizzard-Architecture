package com.student.records.cloud;

import org.springframework.stereotype.Component;

@Component
public class CloudStorageManager {

    public void storeStudentData(String data) {

        System.out.println("Storing student data in cloud storage: " + data);
    }

    public void retrieveStudentData() {

        System.out.println("Retrieving data from cloud storage");
    }
}
