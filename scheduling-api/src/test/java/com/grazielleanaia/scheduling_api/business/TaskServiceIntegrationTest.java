package com.grazielleanaia.scheduling_api.business;


import com.grazielleanaia.scheduling_api.business.dto.CustomerResponseDTO;
import com.grazielleanaia.scheduling_api.business.dto.TaskEvent;
import com.grazielleanaia.scheduling_api.business.dto.TaskRequestDTO;
import com.grazielleanaia.scheduling_api.business.mapper.TaskConverter;
import com.grazielleanaia.scheduling_api.controller.CustomerGateway;
import com.grazielleanaia.scheduling_api.infrastructure.entity.TaskEntity;
import com.grazielleanaia.scheduling_api.infrastructure.repository.TaskRepository;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@Testcontainers

@SpringBootTest(properties = {
        "client.type=http",
        "spring.kafka.bootstrap-servers=localhost:9092",
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JacksonJsonSerializer",
        "spring.kafka.producer.acks=all",
        "spring.kafka.producer.properties.enable.idempotence=true",
        "spring.kafka.producer.properties.max.in.flight.requests.per.connection=5",
        "spring.kafka.producer.properties.delivery.timeout.ms=120000",
        "spring.kafka.producer.properties.linger.ms=0",
        "spring.kafka.producer.properties.request.timeout.ms=30000",
        "management.health.mongo.enabled=true"})

@ActiveProfiles("test")

public class TaskServiceIntegrationTest {

    @Value("${app.kafka.topic.replication-factor:3}")
    private int replicationFactor;

    @Value("${app.kafka.topic.min-insync-replicas:2}")
    private String minInsyncReplicas;

    @Container
    static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Container
    static MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("spring.mongodb.database", () -> "scheduling-test");
        registry.add("eureka.client.enabled", () -> "false");
        registry.add("spring.cloud.config.enabled", () -> "false");
//        registry.add("spring.cloud.bus.enabled", () -> "false");
    }

    @Autowired
    private TaskService taskService;

    @MockitoBean
    private CustomerGateway customerGateway;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskConverter taskConverter;

    @Test
    void testCreateTaskWhenGivenValidTaskRequestSuccessfulSendsKafkaMessage() throws ExecutionException, InterruptedException {

        CustomerResponseDTO customer = new CustomerResponseDTO();
        String customerEmail = "test@email.com";

        when(customerGateway.findCustomerByEmail(customerEmail)).thenReturn(customer);

        TaskRequestDTO request = new TaskRequestDTO();
        request.setCustomerEmail(customerEmail);
        request.setTaskName("Test Task");
        request.setDescription("Test Description");
        request.setEventDate(Instant.now());

        //The real Kafka event is sent here
        taskService.createTask(request, customerEmail);

        TaskEntity taskEntity = taskConverter.toTaskEntity(request);
        taskEntity.setCustomerEmail(customerEmail);
        taskRepository.save(taskEntity);

        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        consumerProps.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, TaskEvent.class);
        consumerProps.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        ConsumerFactory<String, TaskEvent> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        Consumer<String, TaskEvent> consumer = consumerFactory.createConsumer();

        try {
            consumer.subscribe(List.of("task-created-topic"));

            ConsumerRecord<String, TaskEvent> record = KafkaTestUtils.getSingleRecord(consumer, "task-created-topic", Duration.ofSeconds(10));

            assertThat(record.value().getCustomerEmail()).isEqualTo("test@email.com");
            assertThat(record.value().getTaskName()).isEqualTo("Test Task");
            assertThat(record.headers().lastHeader("messageHeaderId")).isNotNull();
        } finally {
            consumer.close();
        }
    }

    @Bean
    @Primary
    NewTopic newTopic() {
        return TopicBuilder.name("task-created-topic")
                .partitions(3)
                .replicas(replicationFactor)
                .configs(Map.of("min.insync.replicas", minInsyncReplicas))
                .build();
    }
}
