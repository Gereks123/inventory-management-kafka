package com.energia.inventory_management.exception;

import lombok.Getter;

@Getter
public class InsufficientStockException extends RuntimeException {
    private final int availableQuantity;
    private final int requestedQuantity;
    private final Long itemId;

    public InsufficientStockException(int availableQuantity, int requestedQuantity, Long itemId) {
        super(String.format("Insufficient stock for id: %d. Available: %d, Requested: %d", itemId, availableQuantity, requestedQuantity));
        this.availableQuantity = availableQuantity;
        this.requestedQuantity = requestedQuantity;
        this.itemId = itemId;
    }
}
