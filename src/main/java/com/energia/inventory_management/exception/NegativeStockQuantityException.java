package com.energia.inventory_management.exception;

import lombok.Getter;

@Getter
public class NegativeStockQuantityException extends RuntimeException {
    private final int quantity;

    public NegativeStockQuantityException(int quantity) {
        super(String.format("Negative stock quantity: %d", quantity));
        this.quantity = quantity;
    }
}
