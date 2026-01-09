package com.superdupermart.shopping.service.impl;

import com.superdupermart.shopping.dao.ProductDao;
import com.superdupermart.shopping.dto.ProductRequest;
import com.superdupermart.shopping.dto.ProductResponse;
import com.superdupermart.shopping.entity.Product;
import com.superdupermart.shopping.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductDao productDao;

    @Autowired
    public ProductServiceImpl(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    @Cacheable(value = "products", key = "#isAdmin")
    public List<ProductResponse> getAllProducts(boolean isAdmin) {
        List<Product> products = isAdmin ? productDao.getAllProducts() : productDao.getInStockProducts();
        return products.stream()
                .map(p -> mapToResponse(p, isAdmin))
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProductById(Integer id, boolean isAdmin) {
        Product product = productDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (!isAdmin && product.getQuantity() <= 0) {
            throw new RuntimeException("Product is out of stock");
        }
        
        return mapToResponse(product, isAdmin);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void addProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .wholesalePrice(request.getWholesalePrice())
                .retailPrice(request.getRetailPrice())
                .quantity(request.getQuantity())
                .build();
        productDao.save(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void updateProduct(Integer id, ProductRequest request) {
        Product product = productDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setWholesalePrice(request.getWholesalePrice());
        product.setRetailPrice(request.getRetailPrice());
        product.setQuantity(request.getQuantity());
        
        productDao.update(product);
    }

    private ProductResponse mapToResponse(Product product, boolean isAdmin) {
        ProductResponse.ProductResponseBuilder builder = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .retailPrice(product.getRetailPrice());

        if (isAdmin) {
            builder.wholesalePrice(product.getWholesalePrice())
                   .quantity(product.getQuantity());
        }

        return builder.build();
    }
}
