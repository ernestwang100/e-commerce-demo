package com.superdupermart.shopping.service.impl;

import com.superdupermart.shopping.dao.ProductDao;
import com.superdupermart.shopping.dto.ProductRequest;
import com.superdupermart.shopping.dto.ProductResponse;
import com.superdupermart.shopping.entity.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductDao productDao;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void getAllProducts_Admin() {
        Product p1 = Product.builder().id(1).name("P1").quantity(10).build();
        when(productDao.getAllProducts()).thenReturn(Arrays.asList(p1));

        List<ProductResponse> result = productService.getAllProducts(true);

        assertEquals(1, result.size());
        verify(productDao).getAllProducts();
        verify(productDao, never()).getInStockProducts();
    }

    @Test
    void getAllProducts_User() {
        Product p1 = Product.builder().id(1).name("P1").quantity(10).build();
        when(productDao.getInStockProducts()).thenReturn(Arrays.asList(p1));

        List<ProductResponse> result = productService.getAllProducts(false);

        assertEquals(1, result.size());
        verify(productDao).getInStockProducts();
        verify(productDao, never()).getAllProducts();
    }

    @Test
    void getProductById_Found() {
        Product p1 = Product.builder().id(1).name("P1").quantity(10).build();
        when(productDao.findById(1)).thenReturn(Optional.of(p1));

        ProductResponse result = productService.getProductById(1, false);

        assertNotNull(result);
        assertEquals("P1", result.getName());
    }

    @Test
    void getProductById_NotFound() {
        when(productDao.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.getProductById(1, false));
    }

    @Test
    void getProductById_OutOfStock_User() {
        Product p1 = Product.builder().id(1).name("P1").quantity(0).build();
        when(productDao.findById(1)).thenReturn(Optional.of(p1));

        assertThrows(RuntimeException.class, () -> productService.getProductById(1, false));
    }

    @Test
    void getProductById_OutOfStock_Admin() {
        Product p1 = Product.builder().id(1).name("P1").quantity(0).build();
        when(productDao.findById(1)).thenReturn(Optional.of(p1));

        ProductResponse result = productService.getProductById(1, true);

        assertNotNull(result);
        assertEquals("P1", result.getName());
    }

    @Test
    void addProduct() {
        ProductRequest request = ProductRequest.builder()
                .name("New Product")
                .quantity(10)
                .retailPrice(BigDecimal.TEN)
                .build();

        productService.addProduct(request);

        verify(productDao).save(any(Product.class));
    }

    @Test
    void updateProduct() {
        // Arrange
        int productId = 1;
        ProductRequest request = ProductRequest.builder()
                .name("Updated Product")
                .quantity(20)
                .retailPrice(BigDecimal.valueOf(20))
                .build();

        Product existingProduct = Product.builder()
                .id(productId)
                .name("Old Product")
                .quantity(10)
                .retailPrice(BigDecimal.TEN)
                .build();

        when(productDao.findById(productId)).thenReturn(Optional.of(existingProduct));

        // Act
        productService.updateProduct(productId, request);

        // Assert
        verify(productDao).findById(productId);
        verify(productDao).update(any(Product.class)); // or capture argument to assert changes

        // Detailed assertion using ArgumentCaptor is better, but this suffices for
        // basic flow
    }
}
