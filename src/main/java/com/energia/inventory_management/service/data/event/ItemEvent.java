package com.energia.inventory_management.service.data.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemEvent {
    private EventType eventType;
    private Long itemId;
    private String itemName;
    private String action;
    private Object data;
    private LocalDateTime timestamp;
}
