package com.superdupermart.shopping.repository;

import com.superdupermart.shopping.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Integer> {
    Page<ProductDocument> findByNameOrDescription(String name, String description, Pageable pageable);

    // Custom query for price range filtering
    Page<ProductDocument> findByNameAndPriceBetween(String name, BigDecimal minPrice,
            BigDecimal maxPrice, Pageable pageable);
}
