package com.room.app.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@EnableScheduling
public class KeepAliveService {

    private final RestTemplate restTemplate;
    private final String serverUrl;

    public KeepAliveService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
        this.serverUrl = "http://localhost:8080"; // Or use your Koyeb app URL
    }

    @Scheduled(fixedRate = 10 * 60 * 1000) // Every 10 minutes
    public void pingServer() {
        try {
            String response = restTemplate.getForObject(serverUrl + "/health", String.class);
            System.out.println("Keep-alive ping successful: " + response);
        } catch (Exception e) {
            System.err.println("Keep-alive ping failed: " + e.getMessage());
        }
    }
}