package com.superdupermart.shopping.controller;

import com.superdupermart.shopping.dto.OrderRequest;
import com.superdupermart.shopping.dto.OrderResponse;
import com.superdupermart.shopping.entity.User;
import com.superdupermart.shopping.dao.UserDao;
import com.superdupermart.shopping.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class OrderController {

    private final OrderService orderService;
    private final UserDao userDao;

    @Autowired
    public OrderController(OrderService orderService, UserDao userDao) {
        this.orderService = orderService;
        this.userDao = userDao;
    }

    // User Endpoints
    @PostMapping("/user/orders")
    public ResponseEntity<OrderResponse> placeOrder(Authentication auth, @RequestBody OrderRequest request) {
        Integer userId = getUserId(auth);
        return ResponseEntity.ok(orderService.placeOrder(userId, request));
    }

    @GetMapping("/user/orders")
    public ResponseEntity<List<OrderResponse>> getUserOrders(Authentication auth) {
        Integer userId = getUserId(auth);
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    @PostMapping("/user/orders/{id}/cancel")
    public ResponseEntity<String> cancelUserOrder(Authentication auth, @PathVariable Integer id) {
        Integer userId = getUserId(auth);
        orderService.cancelOrder(id, userId, false);
        return ResponseEntity.ok("Order canceled successfully");
    }

    // Admin Endpoints
    @GetMapping("/admin/orders")
    public ResponseEntity<List<OrderResponse>> getPaginatedOrders(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(orderService.getPaginatedOrders(page));
    }

    @PostMapping("/admin/orders/{id}/complete")
    public ResponseEntity<String> completeOrder(@PathVariable Integer id) {
        orderService.completeOrder(id);
        return ResponseEntity.ok("Order completed successfully");
    }

    @PostMapping("/admin/orders/{id}/cancel")
    public ResponseEntity<String> cancelAdminOrder(@PathVariable Integer id) {
        orderService.cancelOrder(id, null, true);
        return ResponseEntity.ok("Order canceled by admin successfully");
    }

    private Integer getUserId(Authentication auth) {
        String username = auth.getName();
        return userDao.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
