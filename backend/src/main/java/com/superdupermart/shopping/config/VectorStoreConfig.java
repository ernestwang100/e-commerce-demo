package com.superdupermart.shopping.config;

import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class VectorStoreConfig {

    @Value("${spring.ai.vectorstore.redis.uri}")
    private String redisUri;

    @Value("${spring.ai.vectorstore.redis.index}")
    private String indexName;

    @Value("${spring.ai.vectorstore.redis.prefix}")
    private String prefix;

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "spring.ai.vectorstore.redis.enabled", havingValue = "true", matchIfMissing = true)
    public VectorStore vectorStore(EmbeddingClient embeddingClient) {
        org.springframework.ai.vectorstore.RedisVectorStore.RedisVectorStoreConfig config = org.springframework.ai.vectorstore.RedisVectorStore.RedisVectorStoreConfig
                .builder()
                .withIndexName(indexName)
                .withPrefix(prefix)
                .withURI(redisUri)
                .build();

        return new org.springframework.ai.vectorstore.RedisVectorStore(config, embeddingClient);
    }
}
