package com.energia.inventory_management.service.data.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ModifiedItemDto {
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private Double price;

    @Min(value = 0, message = "Quantity must be greater than or equal to 0")
    private Integer quantity;

    public boolean hasName() {
        return name != null;
    }

    public boolean hasPrice() {
        return price != null;
    }

    public boolean hasQuantity() {
        return quantity != null;
    }
}
