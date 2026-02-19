package com.superdupermart.shopping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Integer orderId;
    private LocalDateTime datePlaced;
    private String orderStatus;
    private List<OrderItemResponse> items;

    // Customer Info
    private Integer userId;
    private String customerUsername;
    private String customerEmail;

    // Delivery Info
    private Boolean isPickup;
    private AddressInfo shippingAddress;

    // Payment Info
    private PaymentInfo paymentMethod;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressInfo {
        private String fullName;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String zipCode;
        private String country;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        private String cardType;
        private String last4Digits;
    }
}
