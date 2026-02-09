package com.superdupermart.shopping.service.impl;

import com.superdupermart.shopping.dao.ProductDao;
import com.superdupermart.shopping.document.ProductDocument;
import com.superdupermart.shopping.dto.PageResponse;
import com.superdupermart.shopping.dto.ProductRequest;
import com.superdupermart.shopping.dto.ProductResponse;
import com.superdupermart.shopping.entity.Product;
import com.superdupermart.shopping.repository.ProductSearchRepository;
import com.superdupermart.shopping.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductDao productDao;
    private final ProductSearchRepository productSearchRepository;

    @Autowired
    public ProductServiceImpl(ProductDao productDao, ProductSearchRepository productSearchRepository) {
        this.productDao = productDao;
        this.productSearchRepository = productSearchRepository;
    }

    @Override
    @Cacheable(value = "products", key = "#isAdmin")
    public List<ProductResponse> getAllProducts(boolean isAdmin) {
        List<Product> products = isAdmin ? productDao.getAllProducts() : productDao.getInStockProducts();
        return products.stream()
                .map(product -> mapToResponse(product, isAdmin))
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<ProductResponse> getProductsPage(int page, int size) {
        List<Product> products = productDao.getPaginatedProducts(page, size);
        long totalElements = productDao.countProducts();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<ProductResponse> content = products.stream()
                .map(product -> mapToResponse(product, true)) // Admin view
                .collect(Collectors.toList());

        return PageResponse.<ProductResponse>builder()
                .content(content)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .size(size)
                .number(page)
                .build();
    }

    @Override
    @Cacheable(value = "product", key = "#id")
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
    @CacheEvict(value = { "products", "product", "product_search" }, allEntries = true)
    public ProductResponse addProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .wholesalePrice(request.getWholesalePrice())
                .retailPrice(request.getRetailPrice())
                .quantity(request.getQuantity())
                .build();
        productDao.save(product);

        // Sync to Elasticsearch
        saveToElasticsearch(product);

        return mapToResponse(product, true);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "products", "product", "product_search" }, allEntries = true)
    public ProductResponse updateProduct(Integer id, ProductRequest request) {
        Product product = productDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setWholesalePrice(request.getWholesalePrice());
        product.setRetailPrice(request.getRetailPrice());
        product.setQuantity(request.getQuantity());

        productDao.update(product);

        // Sync to Elasticsearch
        saveToElasticsearch(product);

        return mapToResponse(product, true);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "product_search", key = "{#query, #minPrice, #maxPrice, #page, #size}")
    public PageResponse<ProductResponse> searchProducts(String query, Double minPrice, Double maxPrice, int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        org.springframework.data.domain.Page<ProductDocument> docsPage;

        if (minPrice != null && maxPrice != null) {
            docsPage = productSearchRepository.findByNameAndPriceBetween(query,
                    java.math.BigDecimal.valueOf(minPrice), java.math.BigDecimal.valueOf(maxPrice), pageable);
        } else if (query != null && !query.trim().isEmpty()) {
            docsPage = productSearchRepository.findByNameOrDescription(query, query, pageable);
        } else {
            docsPage = productSearchRepository.findAll(pageable);
        }

        List<ProductResponse> content = docsPage.getContent().stream()
                .map(this::mapDocumentToResponse)
                .collect(Collectors.toList());

        System.out.println("DEBUG: Search query '" + query + "' returned " + content.size() + " products out of "
                + docsPage.getTotalElements() + " total items.");

        return PageResponse.<ProductResponse>builder()
                .content(content)
                .totalElements(docsPage.getTotalElements())
                .totalPages(docsPage.getTotalPages())
                .size(size)
                .number(page)
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = { "products", "product", "product_search" }, allEntries = true)
    public void uploadProductImage(Integer id, org.springframework.web.multipart.MultipartFile file) {
        Product product = productDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        try {
            product.setImage(file.getBytes());
            product.setImageContentType(file.getContentType());
            productDao.update(product);

            // Sync image metadata to Elasticsearch
            saveToElasticsearch(product);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to upload product image", e);
        }
    }

    @Override
    public Product getProductEntity(Integer id) {
        return productDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    @Transactional
    @CacheEvict(value = { "products", "product", "product_search" }, allEntries = true)
    public void deleteProduct(Integer id) {
        Product product = productDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productDao.delete(id);
        productSearchRepository.deleteById(id);
    }

    // Helper to sync
    private void saveToElasticsearch(Product product) {
        System.out
                .println("DEBUG: Syncing product " + product.getId() + " (" + product.getName() + ") to Elasticsearch");
        ProductDocument doc = ProductDocument.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getRetailPrice())
                .imageContentType(product.getImageContentType())
                .build();
        productSearchRepository.save(doc);
    }

    private ProductResponse mapToResponse(Product product, boolean isAdmin) {
        ProductResponse.ProductResponseBuilder builder = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .retailPrice(product.getRetailPrice())
                .image(product.getImage())
                .imageContentType(product.getImageContentType());

        if (isAdmin) {
            builder.wholesalePrice(product.getWholesalePrice())
                    .quantity(product.getQuantity());
        }

        return builder.build();
    }

    private ProductResponse mapDocumentToResponse(ProductDocument doc) {
        return ProductResponse.builder()
                .id(doc.getId())
                .name(doc.getName())
                .description(doc.getDescription())
                .retailPrice(doc.getPrice())
                // Image data is not in ES, so it will be null in search results list
                // (which is fine for list view, faster). Detail view fetches from DB.
                .imageContentType(doc.getImageContentType())
                .build();
    }

    @Override
    @Transactional
    public void syncAllProducts() {
        List<Product> products = productDao.getAllProducts();
        int count = 0;
        for (Product product : products) {
            saveToElasticsearch(product);
            count++;
        }
        System.out.println("DEBUG: Manually synced " + count + " products to Elasticsearch.");
    }
}
