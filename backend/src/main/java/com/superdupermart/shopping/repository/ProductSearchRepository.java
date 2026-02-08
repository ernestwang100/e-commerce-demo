package com.superdupermart.shopping.repository;

import com.superdupermart.shopping.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Integer> {
    List<ProductDocument> findByNameOrDescription(String name, String description);

    // Custom query for price range filtering
    List<ProductDocument> findByNameAndPriceBetween(String name, BigDecimal minPrice,
            BigDecimal maxPrice);
}
