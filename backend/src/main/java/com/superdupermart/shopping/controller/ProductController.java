package com.superdupermart.shopping.controller;

import com.superdupermart.shopping.dto.ProductRequest;
import com.superdupermart.shopping.dto.ProductResponse;
import com.superdupermart.shopping.service.ProductService;
import com.superdupermart.shopping.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.superdupermart.shopping.security.SecurityUtils;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final StatsService statsService;

    @Autowired
    public ProductController(ProductService productService, StatsService statsService) {
        this.productService = productService;
        this.statsService = statsService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        boolean admin = SecurityUtils.isAdmin();
        // delegating to service which handles admin filter
        return ResponseEntity.ok(productService.getAllProducts(admin));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Integer id) {
        boolean admin = SecurityUtils.isAdmin();
        return ResponseEntity.ok(productService.getProductById(id, admin));
    }

    @PostMapping
    public ResponseEntity<String> addProduct(@RequestBody ProductRequest request) {
        productService.addProduct(request);
        return ResponseEntity.ok("Product added successfully");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable Integer id, @RequestBody ProductRequest request) {
        productService.updateProduct(id, request);
        return ResponseEntity.ok("Product updated successfully");
    }

    @GetMapping("/frequent/{limit}")
    public ResponseEntity<List<String>> getFrequentProducts(@PathVariable Integer limit) {
        Integer userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(statsService.getUserStats(userId).getMostFrequent());
    }

    @GetMapping("/recent/{limit}")
    public ResponseEntity<List<String>> getRecentProducts(@PathVariable Integer limit) {
        Integer userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(statsService.getUserStats(userId).getMostRecent());
    }

    @GetMapping("/profit/{limit}")
    public ResponseEntity<List<?>> getMostProfitable(@PathVariable Integer limit) {
        return ResponseEntity.ok(statsService.getAdminStats().getMostProfitable());
    }

    @GetMapping("/popular/{limit}")
    public ResponseEntity<List<?>> getMostPopular(@PathVariable Integer limit) {
        return ResponseEntity.ok(statsService.getAdminStats().getMostPopular());
    }
}
