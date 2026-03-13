package com.student.records.gateway;

import org.springframework.stereotype.Component;

@Component
public class ApiGateway {

    public void routeRequest(String serviceName) {

        System.out.println("API Gateway routing request to service: " + serviceName);

        switch (serviceName) {

            case "student-service":
                System.out.println("Forwarding request to Student Service");
                break;

            case "edge-node":
                System.out.println("Forwarding request to Edge Node");
                break;

            default:
                System.out.println("Service not found");
        }
    }
}
