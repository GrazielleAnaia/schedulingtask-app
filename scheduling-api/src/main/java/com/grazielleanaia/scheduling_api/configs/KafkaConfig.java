package com.grazielleanaia.scheduling_api.configs;

import com.grazielleanaia.scheduling_api.business.dto.TaskEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;

    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;

    @Value("${spring.kafka.producer.acks}")
    private String acks;

    @Value("${spring.kafka.producer.properties.enable.idempotence}")
    private boolean idempotence;

    @Value("${spring.kafka.producer.properties.max.in.flight.requests.per.connection}")
    private Integer maxInFlightRequestsPerConnection;

    @Value("${spring.kafka.producer.properties.delivery.timeout.ms}")
    private Integer deliveryTimeout;

    @Value("${spring.kafka.producer.properties.linger.ms}")
    private Integer linger;

    @Value("${spring.kafka.producer.properties.request.timeout.ms}")
    private Integer requestTimeout;

    @Bean
    NewTopic createNewTopic() {
        return TopicBuilder.name("task-created-topic")
                .partitions(3)
                .replicas(3)
                .configs(Map.of("min.insync.replicas", "2"))
                .build();
    }

    @Bean
    NewTopic createCancelledTopic() {
        return TopicBuilder.name("task-cancelled-event-topic")
                .partitions(3) //parallelism
                .replicas(3) //fault tolerance
                .configs(Map.of("min.insync.replicas", "2")) //at least 2 brokers must confirm write
                .build();
    }

    Map<String, Object> producerConfigs() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        config.put(ProducerConfig.ACKS_CONFIG, acks);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, idempotence);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, maxInFlightRequestsPerConnection);
        config.put(ProducerConfig.LINGER_MS_CONFIG, linger);
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeout);
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeout);
//        config.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        return config;
    }

    @Bean
    ProducerFactory<String, TaskEvent> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    KafkaTemplate<String, TaskEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
