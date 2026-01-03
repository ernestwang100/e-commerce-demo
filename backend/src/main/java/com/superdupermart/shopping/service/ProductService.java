package com.superdupermart.shopping.service;

import com.superdupermart.shopping.dto.ProductRequest;
import com.superdupermart.shopping.dto.ProductResponse;
import java.util.List;

public interface ProductService {
    List<ProductResponse> getAllProducts(boolean isAdmin);
    ProductResponse getProductById(Integer id, boolean isAdmin);
    void addProduct(ProductRequest request);
    void updateProduct(Integer id, ProductRequest request);
}
