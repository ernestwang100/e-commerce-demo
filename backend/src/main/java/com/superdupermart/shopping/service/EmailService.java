package com.superdupermart.shopping.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username:noreply@superdupermart.com}")
    private String fromEmail;

    private final JavaMailSender emailSender;

    @Autowired
    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Async("taskExecutor")
    public void sendOrderConfirmation(String to, Integer orderId) {
        // Simulate email sending delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Order Confirmation - Order #" + orderId);
        message.setText(
                "Thank you for your order! Your order ID is " + orderId + ". We will notify you when it ships.");

        // In a real app, we would authenticate with an SMTP server.
        // For this demo, we'll just log it if we don't have real credentials,
        // or let the JavaMailSender fail gracefully if configured to doing so.
        // However, standard practice for local dev without a mail server is often
        // just to log the "Send" action.

        System.out.println("Thinking about sending email to " + to + " for Order " + orderId);
        emailSender.send(message);
    }
}
