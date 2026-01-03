package com.superdupermart.shopping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    private Long totalSoldItems;
    private List<ProductStatDto> mostPopular;
    private List<ProductStatDto> mostProfitable;
}
