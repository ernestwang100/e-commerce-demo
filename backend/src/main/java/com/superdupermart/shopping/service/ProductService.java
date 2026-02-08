package com.superdupermart.shopping.service;

import com.superdupermart.shopping.dto.ProductRequest;
import com.superdupermart.shopping.dto.ProductResponse;
import java.util.List;

public interface ProductService {
    List<ProductResponse> getAllProducts(boolean isAdmin);

    com.superdupermart.shopping.dto.PageResponse<ProductResponse> getProductsPage(int page, int size);

    ProductResponse getProductById(Integer id, boolean isAdmin);

    ProductResponse addProduct(ProductRequest request);

    ProductResponse updateProduct(Integer id, ProductRequest request);

    List<ProductResponse> searchProducts(String query, Double minPrice, Double maxPrice);

    void uploadProductImage(Integer id, org.springframework.web.multipart.MultipartFile file);

    com.superdupermart.shopping.entity.Product getProductEntity(Integer id);

    void syncAllProducts();
}
