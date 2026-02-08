package com.superdupermart.shopping.controller;

import com.superdupermart.shopping.dto.ProductRequest;
import com.superdupermart.shopping.dto.ProductResponse;
import com.superdupermart.shopping.service.ProductService;
import com.superdupermart.shopping.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        return ResponseEntity.ok(productService.searchProducts(query, minPrice, maxPrice));
    }

    @GetMapping
    public ResponseEntity<com.superdupermart.shopping.dto.PageResponse<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(productService.getProductsPage(page, size));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> addProduct(@RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.addProduct(request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Integer id,
            @RequestBody ProductRequest request) {
        try {
            return ResponseEntity.ok(productService.updateProduct(id, request));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> uploadProductImage(@PathVariable Integer id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        productService.uploadProductImage(id, file);
        return ResponseEntity.ok("Product image uploaded successfully");
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Integer id) {
        com.superdupermart.shopping.entity.Product product = productService.getProductEntity(id);
        if (product.getImage() == null || product.getImage().length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(product.getImageContentType()))
                .body(product.getImage());
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<?>> getMostProfitable(@PathVariable Integer limit) {
        return ResponseEntity.ok(statsService.getAdminStats().getMostProfitable());
    }

    @GetMapping("/popular/{limit}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<?>> getMostPopular(@PathVariable Integer limit) {
        return ResponseEntity.ok(statsService.getAdminStats().getMostPopular());
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> syncAllProducts() {
        productService.syncAllProducts();
        return ResponseEntity.ok("Products synced to Elasticsearch successfully");
    }
}
