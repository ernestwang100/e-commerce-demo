package com.superdupermart.shopping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private List<OrderItemRequest> order;
    private Integer addressId;
    private com.superdupermart.shopping.entity.Address newAddress;
    private Boolean isPickup;
    private Integer paymentMethodId;
    private com.superdupermart.shopping.entity.PaymentMethod newPaymentMethod;
}
