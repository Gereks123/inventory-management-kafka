package com.energia.inventory_management.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaProperties {
    private String bootstrapServers;
    private Consumer consumer;
    private Producer producer;
    private Listener listener;
    private Admin admin;
    private Topic topic;

    @Data
    public static class Consumer {
        private String groupId;
        private String autoOffsetReset;
        private String keyDeserializer;
        private String valueDeserializer;
        private ConsumerProperties properties = new ConsumerProperties();

        @Data
        public static class ConsumerProperties {
            private String springJsonTrustedPackages;
            private Boolean springJsonUseTypeHeaders;
            private String springJsonTypeMapping;
        }
    }

    @Data
    public static class Producer {
        private String keySerializer;
        private String valueSerializer;
        private String acks;
        private Integer retries;
    }

    @Data
    public static class Listener {
        private String ackMode;
        private Boolean missingTopicsFatal;
        private String type;
    }

    @Data
    public static class Admin {
        private String clientId;
    }

    @Data
    public static class Topic {
        private String sales;
        private String modified;
        private String deleted;
        private String created;
        private String fetch;
        private String salesHistory;
    }
}

