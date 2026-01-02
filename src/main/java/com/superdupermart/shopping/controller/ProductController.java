package com.superdupermart.shopping.controller;

import com.superdupermart.shopping.dto.ProductRequest;
import com.superdupermart.shopping.dto.ProductResponse;
import com.superdupermart.shopping.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // User Endpoints
    @GetMapping("/user/products")
    public ResponseEntity<List<ProductResponse>> getUserProducts() {
        return ResponseEntity.ok(productService.getAllProducts(false));
    }

    @GetMapping("/user/products/{id}")
    public ResponseEntity<ProductResponse> getUserProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductById(id, false));
    }

    // Admin Endpoints
    @GetMapping("/admin/products")
    public ResponseEntity<List<ProductResponse>> getAdminProducts() {
        return ResponseEntity.ok(productService.getAllProducts(true));
    }

    @GetMapping("/admin/products/{id}")
    public ResponseEntity<ProductResponse> getAdminProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductById(id, true));
    }

    @PostMapping("/admin/products")
    public ResponseEntity<String> addProduct(@RequestBody ProductRequest request) {
        productService.addProduct(request);
        return ResponseEntity.ok("Product added successfully");
    }

    @PutMapping("/admin/products/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable Integer id, @RequestBody ProductRequest request) {
        productService.updateProduct(id, request);
        return ResponseEntity.ok("Product updated successfully");
    }
}
