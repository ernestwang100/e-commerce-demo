package com.superdupermart.shopping.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.superdupermart.shopping.security.SecurityConfig;
import com.superdupermart.shopping.dto.AdminStatsResponse;
import com.superdupermart.shopping.dto.ProductRequest;
import com.superdupermart.shopping.dto.ProductResponse;
import com.superdupermart.shopping.dto.UserStatsResponse;
import com.superdupermart.shopping.security.AuthUserDetail;
import com.superdupermart.shopping.security.JwtFilter;
import com.superdupermart.shopping.security.JwtProvider;
import com.superdupermart.shopping.service.ProductService;
import com.superdupermart.shopping.service.StatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import({ SecurityConfig.class, JwtFilter.class })
class ProductControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ProductService productService;

        @MockBean
        private StatsService statsService;

        @MockBean
        private JwtProvider jwtProvider;

        @Test
        void getAllProducts_Anonymous() throws Exception {
                when(productService.getAllProducts(false)).thenReturn(Collections.emptyList());

                mockMvc.perform(get("/products/all"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());

                verify(productService).getAllProducts(false);
        }

        @Test
        void getAllProducts_Admin() throws Exception {
                AuthUserDetail adminSearcher = new AuthUserDetail(1, "admin", "pass",
                                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

                when(productService.getAllProducts(true)).thenReturn(Collections.emptyList());

                mockMvc.perform(get("/products/all")
                                .with(user(adminSearcher)))
                                .andExpect(status().isOk());

                verify(productService).getAllProducts(true);
        }

        @Test
        void getProductById_Found() throws Exception {
                ProductResponse response = ProductResponse.builder()
                                .id(1)
                                .name("Test Product")
                                .build();
                when(productService.getProductById(eq(1), anyBoolean())).thenReturn(response);

                mockMvc.perform(get("/products/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Test Product"));
        }

        @Test
        void addProduct_AsAdmin() throws Exception {
                ProductRequest request = ProductRequest.builder()
                                .name("New Product")
                                .retailPrice(BigDecimal.TEN)
                                .build();

                AuthUserDetail adminUser = new AuthUserDetail(1, "admin", "pass",
                                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

                mockMvc.perform(post("/products")
                                .with(user(adminUser))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());

                verify(productService).addProduct(any(ProductRequest.class));
        }

        @Test
        void addProduct_AsUser_Forbidden() throws Exception {
                ProductRequest request = ProductRequest.builder().name("New Product").build();

                AuthUserDetail normalUser = new AuthUserDetail(2, "user", "pass",
                                List.of(new SimpleGrantedAuthority("ROLE_USER")));

                mockMvc.perform(post("/products")
                                .with(user(normalUser))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isForbidden());
        }

        @Test
        void getFrequentProducts() throws Exception {
                // Mocking user utils
                AuthUserDetail currentUser = new AuthUserDetail(10, "shopper", "pass",
                                List.of(new SimpleGrantedAuthority("ROLE_USER")));

                // Mocking stats service
                UserStatsResponse statsResponse = new UserStatsResponse();
                // Assuming UserStatsResponse has setter or public field or we can mock it.
                // If it's a DTO without setters/constructor, this might fail.
                // Based on other DTOs (ProductRequest) using Lombok @Data @Builder, hopefully
                // this one does too.
                // Let's assume setters or builders exist. For now checking if we can just
                // return a mock?
                // Mocking the DTO is risky if it's final or strictly typed.
                // Better to try to instantiate. If fails, we'll fix.
                // statsResponse.setMostFrequent(List.of("Apple", "Banana"));

                // Actually, let's mock the service to return a Mock of the response if we are
                // unsure about DTO structure.
                UserStatsResponse mockStats = org.mockito.Mockito.mock(UserStatsResponse.class);
                when(mockStats.getMostFrequent()).thenReturn(List.of("Apple", "Banana"));
                when(statsService.getUserStats(10)).thenReturn(mockStats);

                mockMvc.perform(get("/products/frequent/5")
                                .with(user(currentUser)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0]").value("Apple"));
        }
}
