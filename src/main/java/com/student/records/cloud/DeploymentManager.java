package com.student.records.cloud;

import org.springframework.stereotype.Component;

@Component
public class DeploymentManager {

    public void deployService(String serviceName) {

        System.out.println("Deploying service to cloud: " + serviceName);
    }

    public void scaleService(String serviceName) {

        System.out.println("Scaling cloud service: " + serviceName);
    }
}
