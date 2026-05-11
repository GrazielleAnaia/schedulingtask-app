package com.grazielleanaia.scheduling_api.business;

import com.grazielleanaia.scheduling_api.business.dto.TaskEvent;
import com.grazielleanaia.scheduling_api.business.dto.TaskRequestDTO;
import com.grazielleanaia.scheduling_api.business.mapper.TaskConverter;
import com.grazielleanaia.scheduling_api.controller.CustomerGateway;
import com.grazielleanaia.scheduling_api.infrastructure.client.CustomerClient;
import com.grazielleanaia.scheduling_api.infrastructure.exception.CustomerServiceUnavailableException;
import com.grazielleanaia.scheduling_api.infrastructure.repository.TaskRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskConverter taskConverter;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private CustomerGateway customerGateway;

    @Mock
    private KafkaTemplate<String, TaskEvent> kafkaTemplate;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldNotSaveTaskWhenCustomerServiceIsUnavailable() {
        TaskRequestDTO requestDto = new TaskRequestDTO();
        requestDto.setTaskName("Circuit breaker test task");
        requestDto.setDescription("Should not be saved");
        requestDto.setEventDate(Instant.now().plusSeconds(86400));

        when(customerGateway.findCustomerByEmail("test@email.com"))
                .thenThrow(new CustomerServiceUnavailableException(
                        "Customer service is unavailable. Could not verify customer email: test@email.com",
                        new RuntimeException("registration-api is down")));

        assertThrows(CustomerServiceUnavailableException.class, () ->
                taskService.createTask(requestDto, "test@email.com"));

        verify(customerGateway, times(1)).findCustomerByEmail("test@email.com");

        verify(taskRepository, never()).save(any());

        verify(kafkaTemplate, never()).send(any(ProducerRecord.class));
    }
}
