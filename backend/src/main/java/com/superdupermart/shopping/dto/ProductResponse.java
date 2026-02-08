package com.superdupermart.shopping.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String name;
    private String description;
    private BigDecimal retailPrice;

    // Admin only fields
    private BigDecimal wholesalePrice;
    private Integer quantity;
    private byte[] image;
    private String imageContentType;

    public static ProductResponse fromEntity(com.superdupermart.shopping.entity.Product product, boolean isAdmin) {
        ProductResponseBuilder builder = ProductResponse.builder()
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
}
