package com.superdupermart.shopping;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ShoppingAppApplicationTests {

    @Test
    void contextLoads() {
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfig {
        @org.springframework.context.annotation.Bean
        public org.springframework.data.redis.connection.RedisConnectionFactory redisConnectionFactory() {
            return org.mockito.Mockito.mock(org.springframework.data.redis.connection.RedisConnectionFactory.class);
        }

        @org.springframework.context.annotation.Bean
        public org.springframework.ai.vectorstore.VectorStore vectorStore() {
            return org.mockito.Mockito.mock(org.springframework.ai.vectorstore.VectorStore.class);
        }

        @org.springframework.context.annotation.Bean
        public com.superdupermart.shopping.repository.ProductSearchRepository productSearchRepository() {
            return org.mockito.Mockito.mock(com.superdupermart.shopping.repository.ProductSearchRepository.class);
        }
    }

}
