package com.superdupermart.shopping.service;

import com.superdupermart.shopping.dto.OrderRequest;
import com.superdupermart.shopping.dto.OrderResponse;
import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(Integer userId, OrderRequest request);
    void cancelOrder(Integer orderId, Integer userId, boolean isAdmin);
    void completeOrder(Integer orderId);
    List<OrderResponse> getOrdersByUser(Integer userId);
    List<OrderResponse> getPaginatedOrders(int page);
}
