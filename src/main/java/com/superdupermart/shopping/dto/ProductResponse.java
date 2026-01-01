package com.superdupermart.shopping.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal retailPrice;
    
    // Admin only fields
    private BigDecimal wholesalePrice;
    private Integer quantity;
}
