package com.energia.inventory_management.service.kafka;

import com.energia.inventory_management.service.data.event.QueryEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.energia.inventory_management.service.data.event.ItemEvent;


@Slf4j
@Service
public class EventSubscriberService {
    @KafkaListener(topics = "items-sold", containerFactory = "generalKafkaListenerContainerFactory", groupId = "${spring.kafka.consumer.group-id}")
    public void handleSoldEvent(ItemEvent event) {
        log.info("Received sold event: {}", event);
    }

    @KafkaListener(topics = "items-changed", containerFactory = "generalKafkaListenerContainerFactory", groupId = "${spring.kafka.consumer.group-id}")
    public void handleChangedEvent(ItemEvent event) {
        log.info("Received changed event: {}", event);
    }

    @KafkaListener(topics = "items-deleted", containerFactory = "generalKafkaListenerContainerFactory", groupId = "${spring.kafka.consumer.group-id}")
    public void handleDeletedEvent(ItemEvent event) {
        log.info("Received deleted event: {}", event);
    }

    @KafkaListener(topics = "items-created", containerFactory = "generalKafkaListenerContainerFactory", groupId = "${spring.kafka.consumer.group-id}")
    public void handleCreatedEvent(ItemEvent event) {
        log.info("Received created event: {}", event);
    }

    @KafkaListener(topics = "items-fetched", containerFactory = "generalKafkaListenerContainerFactory", groupId = "${spring.kafka.consumer.group-id}")
    public void handleFetchedEvent(QueryEvent event) {
        log.info("Received fetched event: {}", event);
    }

    @KafkaListener(topics = "items-sales-history", containerFactory = "generalKafkaListenerContainerFactory", groupId = "${spring.kafka.consumer.group-id}")
    public void handleSalesHistoryEvent(QueryEvent event) {
        log.info("Sales history queried with parameters: {}", event.getParameters());
    }
}
