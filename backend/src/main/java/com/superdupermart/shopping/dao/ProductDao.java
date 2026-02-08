package com.superdupermart.shopping.dao;

import com.superdupermart.shopping.entity.Product;
import java.util.List;
import java.util.Optional;

public interface ProductDao {
    Optional<Product> findById(Integer id);

    List<Product> getAllProducts();

    List<Product> getInStockProducts();

    void save(Product product);

    void update(Product product);

    List<Product> searchProducts(String query, Double minPrice, Double maxPrice);

    List<Product> getPaginatedProducts(int page, int size);

    long countProducts();
}
