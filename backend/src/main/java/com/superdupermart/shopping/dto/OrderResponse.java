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
}
