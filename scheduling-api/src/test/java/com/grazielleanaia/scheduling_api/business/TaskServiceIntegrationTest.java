package com.grazielleanaia.scheduling_api.business;

/*
I used Testcontainers to run real Kafka and MongoDB during integration tests.
The test starts the Spring Boot application with the test profile,
replaces only the external customer service dependency with a mock,
calls the real TaskService, persists through the real Mongo repository,
publishes through the real KafkaTemplate/KafkaConfig,
and verifies the produced Kafka event using a real consumer
*/

import com.grazielleanaia.scheduling_api.business.dto.CustomerResponseDTO;
import com.grazielleanaia.scheduling_api.business.dto.TaskEvent;
import com.grazielleanaia.scheduling_api.business.dto.TaskRequestDTO;
import com.grazielleanaia.scheduling_api.business.dto.TaskResponseDTO;
import com.grazielleanaia.scheduling_api.controller.CustomerGateway;
import com.grazielleanaia.scheduling_api.infrastructure.entity.TaskEntity;
import com.grazielleanaia.scheduling_api.infrastructure.exception.CustomerServiceUnavailableException;
import com.grazielleanaia.scheduling_api.infrastructure.repository.TaskRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private static final String TASK_CREATED_TOPIC = "task-created-topic";

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
    }

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @MockitoBean
    private CustomerGateway customerGateway;

    private Consumer<String, TaskEvent> consumer;


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

        try (Consumer<String, TaskEvent> testConsumer = createTaskEventConsumer()) {
            testConsumer.subscribe(List.of(TASK_CREATED_TOPIC));

            //Consumer starts listening to your producer topic
            ConsumerRecord<String, TaskEvent> record =
                    KafkaTestUtils.getSingleRecord(testConsumer, TASK_CREATED_TOPIC, Duration.ofSeconds(10));

            //Proves the payload has the expected email
            assertThat(record.value().getCustomerEmail()).isEqualTo("test@email.com");

            assertThat(record.value().getTaskName()).isEqualTo("Test Task");

            //Proves producer added the idempotency tracking header
            assertThat(record.headers().lastHeader("messageHeaderId")).isNotNull();
        }
    }

    @Test
    void shouldPublishTaskCreatedEventWithCorrectPayload() throws ExecutionException, InterruptedException {
        CustomerResponseDTO customer = new CustomerResponseDTO();
        String customerEmail = "test@email.com";

        when(customerGateway.findCustomerByEmail(customerEmail)).thenReturn(customer);

        TaskRequestDTO request = new TaskRequestDTO();
        request.setCustomerEmail(customerEmail);
        request.setTaskName("Test Task");
        request.setDescription("Test Description");
        request.setEventDate(Instant.now());

        TaskResponseDTO responseDTO = taskService.createTask(request, customerEmail);

        try (Consumer<String, TaskEvent> testConsumer = createTaskEventConsumer()) {
            testConsumer.subscribe(List.of(TASK_CREATED_TOPIC));

            ConsumerRecord<String, TaskEvent> record =
                    KafkaTestUtils.getSingleRecord(testConsumer, TASK_CREATED_TOPIC, Duration.ofSeconds(10));

            TaskEvent event = record.value();

            //topic and key
            assertThat(record.topic()).isEqualTo("task-created-topic");
            assertThat(record.key()).isEqualTo(responseDTO.getId());

            //payload
            assertThat(event.getTaskId()).isEqualTo(responseDTO.getId());
            assertThat(event.getCustomerEmail()).isEqualTo(customerEmail);
            assertThat(event.getTaskName()).isEqualTo("Test Task");
            assertThat(event.getStatus()).isEqualTo("PENDING");
            assertThat(event.getEventDate()).isEqualTo(request.getEventDate());

            //headers
            assertThat(record.headers().lastHeader("messageHeaderId")).isNotNull();
        }
    }

    @Test
    void shouldNotPublishKafkaMessageWhenCustomerValidationFails() {
        String customerEmail = "test@email.com";

        TaskRequestDTO request = new TaskRequestDTO();
        request.setCustomerEmail(customerEmail);
        request.setTaskName("Test Task");
        request.setDescription("Test Description");
        request.setEventDate(Instant.now());

        when(customerGateway.findCustomerByEmail(customerEmail)).thenThrow(new CustomerServiceUnavailableException(
                "Customer service is unavailable. Could not verify your email."));

        assertThatThrownBy(() -> taskService.createTask(request, customerEmail))
                .isInstanceOf(CustomerServiceUnavailableException.class);

        ConsumerRecords<String, TaskEvent> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(3));

        assertThat(records.count()).isZero();
    }

    @Test
    void shouldSaveTaskInMongoAndPublishMatchingKafkaEvent() throws Exception {
        String customerEmail = "test@email.com";
        Instant eventDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        when(customerGateway.findCustomerByEmail(customerEmail))
                .thenReturn(new CustomerResponseDTO());

        TaskRequestDTO request = new TaskRequestDTO();
        request.setCustomerEmail(customerEmail);
        request.setTaskName("Test Task");
        request.setDescription("Test Description");
        request.setEventDate(eventDate);

        //Calls the real service TaskService
        TaskResponseDTO response = taskService.createTask(request, customerEmail);

        try (Consumer<String, TaskEvent> testConsumer = createTaskEventConsumer()) {
            testConsumer.subscribe(List.of(TASK_CREATED_TOPIC));

            ConsumerRecord<String, TaskEvent> record = KafkaTestUtils.getSingleRecord(
                    testConsumer,
                    TASK_CREATED_TOPIC,
                    Duration.ofSeconds(10));

            //Saves into the real MongoDB Testcontainer
            Optional<TaskEntity> saved = taskRepository.findById(response.getId());

            assertThat(saved).isPresent();

            TaskEntity savedEntity = saved.get();
            TaskEvent event = record.value();

            assertThat(record.key()).isEqualTo(savedEntity.getId());
            assertThat(event.getTaskId()).isEqualTo(savedEntity.getId());
            assertThat(event.getCustomerEmail()).isEqualTo(savedEntity.getCustomerEmail());
            assertThat(event.getTaskName()).isEqualTo(savedEntity.getTaskName());
            assertThat(event.getEventDate().truncatedTo(ChronoUnit.MILLIS)).isEqualTo(savedEntity.getEventDate().truncatedTo(ChronoUnit.MILLIS));
            assertThat(event.getStatus()).isEqualTo("PENDING");
            assertThat(record.headers().lastHeader("messageHeaderId")).isNotNull();
        }
    }

    private Consumer<String, TaskEvent> createTaskEventConsumer() {
        Map<String, Object> consumerProps = new HashMap<>();

        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        consumerProps.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.grazielleanaia.scheduling_api.business.dto");
        consumerProps.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, TaskEvent.class);
        consumerProps.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        ConsumerFactory<String, TaskEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProps);

        return consumerFactory.createConsumer();
    }

}
