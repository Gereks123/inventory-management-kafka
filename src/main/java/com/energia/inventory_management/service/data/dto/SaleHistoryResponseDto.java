package com.energia.inventory_management.service.data.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SaleHistoryResponseDto {
    private Long itemId;
    private Integer quantitySold;
    private Double priceAtSale;
    private LocalDateTime saleDate;
}
