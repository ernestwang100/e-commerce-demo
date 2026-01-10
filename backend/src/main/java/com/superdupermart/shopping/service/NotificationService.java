package com.superdupermart.shopping.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @KafkaListener(topics = "orders", groupId = "shopping-group")
    public void listen(String message) {
        System.out.println("Received order event: " + message);
        // Simulate sending email/SMS
    }
}
