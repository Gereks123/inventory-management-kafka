package com.energia.inventory_management.controller;

import com.energia.inventory_management.exception.EmptyRequestBodyException;
import com.energia.inventory_management.repository.entity.Item;
import com.energia.inventory_management.service.ItemService;
import com.energia.inventory_management.service.data.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping( "/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/sales")
    public ResponseEntity<PageResponseDto<SaleHistoryResponseDto>> getSalesHistory(
            @RequestParam(required = false) Long itemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return ResponseEntity.ok(itemService.getSalesHistory(itemId, page, size, sortBy, direction));
    }

    @PostMapping
    public ResponseEntity<Item> createNewItem(@Valid @RequestBody ItemDto newItem) {
        if (isEmptyRequest(newItem)) {
            throw new EmptyRequestBodyException("Request body cannot be empty for item creation");
        }
        return ResponseEntity.ok(itemService.addItem(newItem));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> modifyItem(@PathVariable Long id, @Valid @RequestBody ModifiedItemDto modifiedItem) {
        if (isEmptyRequest(modifiedItem)) {
            throw new EmptyRequestBodyException("Request body cannot be empty when modifying an item");
        }
        return ResponseEntity.ok(itemService.updateItem(id, modifiedItem));
    }

    @PatchMapping("/{id}/sell")
    public ResponseEntity<Item> sellItem(@PathVariable Long id, @Valid @RequestBody SaleDto saleItem) {
        if (isEmptyRequest(saleItem)) {
            throw new EmptyRequestBodyException("Request body cannot be empty when selling an item");
        }
        return ResponseEntity.ok(itemService.sellItem(id, saleItem));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponseDto<Item>> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction)
    {
        return ResponseEntity.ok(itemService.getAllItems(page, size, sortBy, direction));
    }

    private boolean isEmptyRequest(Object dto) {
        return dto == null ||
                (dto instanceof ItemDto updateDto &&
                        !updateDto.hasName() &&
                        !updateDto.hasPrice() &&
                        !updateDto.hasQuantity());
    }
}
