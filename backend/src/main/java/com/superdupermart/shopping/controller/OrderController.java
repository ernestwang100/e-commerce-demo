package com.superdupermart.shopping.controller;

import com.superdupermart.shopping.dto.OrderRequest;
import com.superdupermart.shopping.dto.OrderResponse;
import com.superdupermart.shopping.security.SecurityUtils;
import com.superdupermart.shopping.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("Unable to retrieve user ID from authentication context");
        }
        return ResponseEntity.ok(orderService.placeOrder(userId, request));
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllOrders(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        if (SecurityUtils.isAdmin()) {
            return ResponseEntity.ok(orderService.getOrdersPage(page, size));
        } else {
            Integer userId = SecurityUtils.getCurrentUserId();
            // Current Service implementation doesn't have pagination for user orders
            // specifically,
            // but we can filter or just return all for now if small.
            // Aligning with standard paginated response if possible.
            return ResponseEntity.ok(orderService.getOrdersByUser(userId));
        }
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Map<String, String>> cancelOrder(@PathVariable Integer id) {
        boolean isAdmin = SecurityUtils.isAdmin();
        Integer userId = SecurityUtils.getCurrentUserId();
        // If admin, userId doesn't matter for ownership check in service usually, or we
        // pass null/admin flag
        orderService.cancelOrder(id, userId, isAdmin);
        return ResponseEntity.ok(Collections.singletonMap("message", "Order canceled successfully"));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> completeOrder(@PathVariable Integer id) {
        orderService.completeOrder(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "Order completed successfully"));
    }
}
