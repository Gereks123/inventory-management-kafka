package com.energia.inventory_management.config;


import com.energia.inventory_management.service.data.event.ItemEvent;
import com.energia.inventory_management.service.data.event.QueryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private static final String ITEM_EVENT_TYPE_MAPPING = "itemEvent:com.energia.inventory_management.service.data.event.ItemEvent";
    private static final String QUERY_EVENT_TYPE_MAPPING = "queryEvent:com.energia.inventory_management.service.data.event.QueryEvent";
    public static final long DEFAULT_BACKOFF_INTERVAL = 1000L;
    public static final long MAX_RETRY_ATTEMPTS = 2L;

    private final KafkaProperties kafkaProperties;

    @Bean
    public KafkaTemplate<String, Object> generalKafkaTemplate() {
        return new KafkaTemplate<>(generalProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, Object> generalKafkaConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumer().getGroupId());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getConsumer().getAutoOffsetReset());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaProperties.getConsumer().getKeyDeserializer());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaProperties.getConsumer().getValueDeserializer());


        config.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, kafkaProperties.getConsumer().getKeyDeserializer());
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, kafkaProperties.getConsumer().getValueDeserializer());

        config.put(JsonDeserializer.TRUSTED_PACKAGES, kafkaProperties.getConsumer().getProperties().getSpringJsonTrustedPackages());
        config.put(JsonDeserializer.TYPE_MAPPINGS, ITEM_EVENT_TYPE_MAPPING + "," + QUERY_EVENT_TYPE_MAPPING);

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public RecordMessageConverter messageConverter() {
        StringJsonMessageConverter converter = new StringJsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();

        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);
        typeMapper.addTrustedPackages(kafkaProperties.getConsumer().getProperties().getSpringJsonTrustedPackages());

        Map<String, Class<?>> mappings = new HashMap<>();
        mappings.put("itemEvent", ItemEvent.class);
        mappings.put("queryEvent", QueryEvent.class);
        typeMapper.setIdClassMapping(mappings);
        converter.setTypeMapper(typeMapper);

        return converter;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> generalKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setBatchListener(kafkaProperties.getListener().getType().equals("batch"));
        factory.getContainerProperties().setAckMode(
                ContainerProperties.AckMode.valueOf(kafkaProperties.getListener().getAckMode()));

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new FixedBackOff(DEFAULT_BACKOFF_INTERVAL, MAX_RETRY_ATTEMPTS)
        );
        factory.setCommonErrorHandler(errorHandler);

        factory.setConsumerFactory(generalKafkaConsumerFactory());
        factory.setRecordMessageConverter(messageConverter());

        return factory;
    }

    @Bean
    public ProducerFactory<String, Object> generalProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaProperties.getProducer().getKeySerializer());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaProperties.getProducer().getValueSerializer());
        config.put(ProducerConfig.ACKS_CONFIG, kafkaProperties.getProducer().getAcks());
        config.put(ProducerConfig.RETRIES_CONFIG, kafkaProperties.getProducer().getRetries());

        config.put(JsonSerializer.TYPE_MAPPINGS, ITEM_EVENT_TYPE_MAPPING + ", " + QUERY_EVENT_TYPE_MAPPING);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        configs.put(AdminClientConfig.CLIENT_ID_CONFIG, kafkaProperties.getAdmin().getClientId());
        return new KafkaAdmin(configs);
    }
}
