package com.energia.inventory_management.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic itemsSoldTopic() {
        return TopicBuilder.name("items-sold").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic itemsChangedTopic() {
        return TopicBuilder.name("items-changed").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic itemsDeletedTopic() {
        return TopicBuilder.name("items-deleted").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic itemsCreatedTopic() {
        return TopicBuilder.name("items-created").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic itemsFetchedTopic() {
        return TopicBuilder.name("items-fetched").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic itemsSalesHistoryTopic() {
        return TopicBuilder.name("items-sales-history").partitions(3).replicas(1).build();
    }
}
