package com.superdupermart.shopping.service;

import com.superdupermart.shopping.exception.PaymentFailedException;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.Random;

@Service
public class PaymentService {

    private final Random random = new Random();

    /**
     * Simulates authorizing a payment with a provider like Stripe.
     * 
     * @param amount The amount to authorize
     * @return A transaction ID if successful
     * @throws PaymentFailedException if the payment "fails"
     */
    public String authorizeTransaction(Double amount) {
        // Simulate network latency
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 10% change of failure to simulate "Real World" scenarios (Insufficent funds,
        // etc)
        if (random.nextInt(10) == 0) {
            throw new PaymentFailedException("Payment authorization failed: Card declined.");
        }

        return "tx_" + UUID.randomUUID().toString();
    }
}
