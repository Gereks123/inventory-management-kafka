package com.energia.inventory_management.service.data.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryEvent {
    private EventType eventType;
    private Map<String, Object> parameters;
    private LocalDateTime timestamp;
}
