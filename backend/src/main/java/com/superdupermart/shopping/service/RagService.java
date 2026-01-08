package com.superdupermart.shopping.service;

import com.superdupermart.shopping.entity.Product;
import com.superdupermart.shopping.dao.ProductDao;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RagService {

    private static final Logger logger = LoggerFactory.getLogger(RagService.class);

    private final VectorStore vectorStore;
    private final ProductDao productDao;

    @Autowired
    public RagService(VectorStore vectorStore, ProductDao productDao) {
        this.vectorStore = vectorStore;
        this.productDao = productDao;
    }

    @PostConstruct
    public void initProductIndex() {
        logger.info("Initializing Vector Store with products...");
        try {
            List<Product> products = productDao.getAllProducts();
            
            if (products.isEmpty()) {
                logger.warn("No products found to ingest into Vector Store.");
                return;
            }

            List<Document> documents = products.stream()
                    .map(product -> {
                        String content = "Product Name: " + product.getName() + " | " +
                                         "Description: " + product.getDescription() + " | " +
                                         "Price: $" + product.getRetailPrice();
                        
                        Map<String, Object> metadata = Map.of(
                            "id", product.getId(),
                            "name", product.getName(),
                            "price", product.getRetailPrice()
                        );
                        
                        return new Document(content, metadata);
                    })
                    .collect(Collectors.toList());

            vectorStore.add(documents);
            logger.info("Successfully loaded {} products into the Vector Store.", documents.size());
        } catch (Exception e) {
            logger.error("Error initializing Vector Store: ", e);
        }
    }

    public List<Document> retrieveDocuments(String query) {
        logger.info("Retrieving documents for query: {}", query);
        try {
            List<Document> similarDocuments = vectorStore.similaritySearch(query);
            logger.info("Found {} relevant documents.", similarDocuments.size());
            return similarDocuments;
        } catch (Exception e) {
            logger.error("Error retrieving documents: ", e);
            return List.of();
        }
    }
}
