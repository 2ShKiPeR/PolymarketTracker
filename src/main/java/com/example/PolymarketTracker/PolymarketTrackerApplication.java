package com.example.PolymarketTracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PolymarketTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PolymarketTrackerApplication.class, args);
    }
}