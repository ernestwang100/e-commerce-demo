package com.superdupermart.shopping.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_method")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "card_holder", nullable = false)
    private String cardHolder;

    @Column(name = "card_type", nullable = false)
    private String cardType; // e.g., "Visa", "MasterCard"

    @Column(name = "last4", nullable = false)
    private String last4;

    @Column(name = "expiry_date", nullable = false)
    private String expiryDate; // MM/YY

    // In a real app, we might store a token from a Payment Gateway here
    // private String paymentToken;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;
}
