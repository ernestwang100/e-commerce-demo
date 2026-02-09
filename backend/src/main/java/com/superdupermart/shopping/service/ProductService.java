package com.superdupermart.shopping.service;

import com.superdupermart.shopping.dto.ProductRequest;
import com.superdupermart.shopping.dto.ProductResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ProductService {
    List<ProductResponse> getAllProducts(boolean isAdmin);

    com.superdupermart.shopping.dto.PageResponse<ProductResponse> getProductsPage(int page, int size);

    ProductResponse getProductById(Integer id, boolean isAdmin);

    ProductResponse addProduct(ProductRequest request);

    ProductResponse updateProduct(Integer id, ProductRequest request);

    com.superdupermart.shopping.dto.PageResponse<ProductResponse> searchProducts(String query, Double minPrice,
            Double maxPrice, int page, int size);

    void uploadProductImage(Integer id, MultipartFile file); // Changed type to MultipartFile

    com.superdupermart.shopping.entity.Product getProductEntity(Integer id);

    void syncAllProducts(); // Admin only

    void deleteProduct(Integer id);
}
