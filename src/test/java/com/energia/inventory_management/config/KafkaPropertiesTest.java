package com.energia.inventory_management.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Kafka Configuration Properties Tests")
class KafkaPropertiesTest {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String CONSUMER_GROUP_ID = "inventory-group";
    private static final String CONSUMER_OFFSET_RESET = "earliest";
    private static final String TRUSTED_PACKAGES = "com.energia.inventory_management.service.data.event";
    private static final String TYPE_MAPPING = "itemEvent:com.energia.inventory_management.service.data.event.ItemEvent," +
            "queryEvent:com.energia.inventory_management.service.data.event.QueryEvent";

    private static final class SerializerConfig {
        static final String STRING_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
        static final String STRING_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
        static final String JSON_SERIALIZER = "org.springframework.kafka.support.serializer.JsonSerializer";
        static final String JSON_DESERIALIZER = "org.springframework.kafka.support.serializer.JsonDeserializer";
    }

    private static final class TopicNames {
        static final String SALES = "item-sales";
        static final String MODIFIED = "items-modified";
        static final String DELETED = "items-deleted";
        static final String CREATED = "items-created";
        static final String FETCH = "items-fetched";
    }

    @Autowired
    private KafkaProperties kafkaProperties;

    @Nested
    @DisplayName("Basic Configuration Tests")
    class BasicConfigurationTests {

        @Test
        @DisplayName("Should load bootstrap servers configuration")
        void shouldLoadBootstrapServers() {
            assertEquals(BOOTSTRAP_SERVERS, kafkaProperties.getBootstrapServers());
        }

        @Test
        @DisplayName("Should load admin client configuration")
        void shouldLoadAdminConfig() {
            assertNotNull(kafkaProperties.getAdmin());
            assertEquals("inventory-admin", kafkaProperties.getAdmin().getClientId());
        }
    }

    @Nested
    @DisplayName("Consumer Configuration Tests")
    class ConsumerConfigurationTests {

        @Test
        @DisplayName("Should load main consumer properties")
        void should_Load_Main_Consumer_Properties() {
            assertNotNull(kafkaProperties.getConsumer());
            assertEquals(CONSUMER_GROUP_ID, kafkaProperties.getConsumer().getGroupId());
            assertEquals(CONSUMER_OFFSET_RESET, kafkaProperties.getConsumer().getAutoOffsetReset());
        }

        @Test
        @DisplayName("Should load consumer serialization properties")
        void should_Load_Consumer_Serialization_Properties() {
            assertEquals(SerializerConfig.STRING_DESERIALIZER,
                    kafkaProperties.getConsumer().getKeyDeserializer());
            assertEquals(SerializerConfig.JSON_DESERIALIZER,
                    kafkaProperties.getConsumer().getValueDeserializer());
        }

        @Test
        @DisplayName("Should load consumer JSON properties")
        void shouldLoadConsumerJsonProperties() {
            var properties = kafkaProperties.getConsumer().getProperties();
            assertNotNull(properties);
            assertEquals(TRUSTED_PACKAGES, properties.getSpringJsonTrustedPackages());
            assertTrue(properties.getSpringJsonUseTypeHeaders());
            assertEquals(TYPE_MAPPING, properties.getSpringJsonTypeMapping());
        }
    }

    @Nested
    @DisplayName("Producer Configuration Tests")
    class ProducerConfigurationTests {

        @Test
        @DisplayName("Should load producer properties")
        void shouldLoadProducerProperties() {
            assertNotNull(kafkaProperties.getProducer());
            assertEquals(SerializerConfig.STRING_SERIALIZER,
                    kafkaProperties.getProducer().getKeySerializer());
            assertEquals(SerializerConfig.JSON_SERIALIZER,
                    kafkaProperties.getProducer().getValueSerializer());
            assertEquals("all", kafkaProperties.getProducer().getAcks());
            assertEquals(3, kafkaProperties.getProducer().getRetries());
        }
    }

    @Nested
    @DisplayName("Listener Configuration Tests")
    class ListenerConfigurationTests {

        @Test
        @DisplayName("Should load listener properties")
        void shouldLoadListenerProperties() {
            assertNotNull(kafkaProperties.getListener());
            assertEquals("MANUAL_IMMEDIATE", kafkaProperties.getListener().getAckMode());
            assertFalse(kafkaProperties.getListener().getMissingTopicsFatal());
            assertEquals("batch", kafkaProperties.getListener().getType());
        }
    }

    @Nested
    @DisplayName("Topic Configuration Tests")
    class TopicConfigurationTests {

        @Test
        @DisplayName("Should load all topic names")
        void shouldLoadTopicNames() {
            assertNotNull(kafkaProperties.getTopic());
            assertEquals(TopicNames.SALES, kafkaProperties.getTopic().getSales());
            assertEquals(TopicNames.MODIFIED, kafkaProperties.getTopic().getModified());
            assertEquals(TopicNames.DELETED, kafkaProperties.getTopic().getDeleted());
            assertEquals(TopicNames.CREATED, kafkaProperties.getTopic().getCreated());
            assertEquals(TopicNames.FETCH, kafkaProperties.getTopic().getFetch());
        }
    }
}