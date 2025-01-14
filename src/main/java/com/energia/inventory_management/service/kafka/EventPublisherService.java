package com.energia.inventory_management.service.kafka;

import com.energia.inventory_management.service.data.event.EventType;
import com.energia.inventory_management.service.data.event.ItemEvent;
import com.energia.inventory_management.service.data.event.QueryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final KafkaTemplate<String, Object> kafkaTemplate;


    public void publishEvent(String topic, ItemEvent event) {
        log.info("Publishing event: {} to topic: {}", event, topic);
        kafkaTemplate.send(topic, event);
    }

    public void publishQueryEvent(String topic, QueryEvent event) {
        log.info("Publishing query event: {} to topic: {}", event, topic);
        kafkaTemplate.send(topic, event);
    }

    public QueryEvent createQueryEvent(EventType eventType, Map<String, Object> parameters) {
        return QueryEvent.builder()
                .eventType(eventType)
                .parameters(parameters)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public ItemEvent createEvent(
            EventType eventType,
            Long itemId,
            String itemName,
            String action,
            Object data)
    {
        return ItemEvent.builder()
                .eventType(eventType)
                .itemId(itemId)
                .itemName(itemName)
                .action(action)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
