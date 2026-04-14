package com.grazielleanaia.scheduling_api.business;

import com.grazielleanaia.scheduling_api.business.dto.*;
import com.grazielleanaia.scheduling_api.business.mapper.TaskConverter;
import com.grazielleanaia.scheduling_api.controller.CustomerGateway;
import com.grazielleanaia.scheduling_api.infrastructure.client.CustomerClient;
import com.grazielleanaia.scheduling_api.infrastructure.entity.TaskEntity;
import com.grazielleanaia.scheduling_api.infrastructure.enums.NotificationStatusEnum;
import com.grazielleanaia.scheduling_api.infrastructure.exception.ResourceNotFoundException;
import com.grazielleanaia.scheduling_api.infrastructure.repository.TaskRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RefreshScope
@Service

public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskConverter taskConverter;

    private final MongoTemplate mongoTemplate;

    private final CustomerClient customerClient;

    private final CustomerGateway customerGateway;

    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;

    private Logger logger = LoggerFactory.getLogger(TaskService.class);

    public TaskService(TaskRepository taskRepository, TaskConverter taskConverter,
                       MongoTemplate mongoTemplate, CustomerClient customerClient,
                       KafkaTemplate<String, TaskEvent> kafkaTemplate,
                       CustomerGateway customerGateway) {
        this.taskRepository = taskRepository;
        this.taskConverter = taskConverter;
        this.mongoTemplate = mongoTemplate;
        this.customerClient = customerClient;
        this.kafkaTemplate = kafkaTemplate;
        this.customerGateway = customerGateway;
    }

    //Ok
    @Transactional
    public TaskResponseDTO createTask(TaskRequestDTO request, Long customerId) throws ExecutionException, InterruptedException {
        //Validate customer exists with FeignClient
        CustomerResponseDTO customerResponseDTO = customerGateway.findCustomerById(customerId); //customerGateway decides feign or http client type

        TaskEntity entity = taskConverter.toTaskEntity(request);
        entity.setCustomerId(customerId);
        entity.setNotificationStatusEnum(NotificationStatusEnum.PENDING);
        entity.setDeleted(false);
        entity.setTaskName(request.getTaskName());
        entity.setDescription(request.getDescription());
        entity.setEventDate(request.getEventDate());
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        TaskEntity savedEntity = taskRepository.save(entity);
        TaskEvent event = new TaskEvent(savedEntity.getId(), customerId, savedEntity.getTaskName(),
                savedEntity.getEventDate(), "PENDING");

        //Send message synchronously
        ProducerRecord<String, TaskEvent> record = new ProducerRecord<>("task-created-topic", event.getTaskId(), event);
        record.headers().add(new RecordHeader("messageHeaderId", UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)));

        SendResult<String, TaskEvent> result = kafkaTemplate.send(record).get();
        logger.info("Partition: " + result.getRecordMetadata().partition());
        logger.info("Topic name: " + result.getRecordMetadata().topic());
        logger.info("Offset: " + result.getRecordMetadata().offset());
        logger.info("Has offset: " + result.getRecordMetadata().hasOffset());
        logger.info("Serialized key size: " + result.getRecordMetadata().serializedKeySize());
        logger.info("Serialized value size: " + result.getRecordMetadata().serializedValueSize());
        logger.info("Timestamp: " + result.getRecordMetadata().timestamp());
        logger.info("---sending to task-created-topic---" + event);

        return taskConverter.toTaskResponseDTO(savedEntity);
    }

    //Ok
    @Transactional
    public void softDeleteTask(String id, Long customerId) {
        TaskEntity entity = taskRepository.findByIdAndCustomerIdAndDeletedFalse(id,
                        customerClient.findCustomerById(customerId).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
        entity.setDeleted(true);
        entity.setUpdatedAt(Instant.now());
        entity.setNotificationStatusEnum(NotificationStatusEnum.CANCELLED);
        taskRepository.save(entity);

        TaskEvent event = new TaskEvent(entity.getId(), customerId, entity.getTaskName(),
                entity.getEventDate(), "CANCELLED");
        kafkaTemplate.send("task-cancelled-event-topic", event);
        logger.info("sending to task-cancelled-event-topic" + event);
    }

    @Transactional(readOnly = true)
    public TaskResponse findByPeriodAndPendingTask(Long customerId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder,
                                                   Instant initialDate, Instant finalDate) {

        if (initialDate.isAfter(finalDate)) {
            throw new IllegalArgumentException("initial date must be before final date");
        }
        Sort.Direction sortDirection = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Direction.ASC;
        Sort sort = Sort.by(sortDirection, sortBy);
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);

        Page<TaskEntity> taskPage = taskRepository.findByCustomerIdAndEventDateBetweenAndNotificationStatusEnum(customerId, initialDate, finalDate,
                NotificationStatusEnum.PENDING, pageDetails);
        List<TaskEntity> taskResponseDTOList = taskPage.getContent();
        List<TaskResponseDTO> content = taskResponseDTOList.stream()
                .map(taskConverter::toTaskResponseDTO)
                .toList();
        TaskResponse response = new TaskResponse();
        response.setTasks(content);
        response.setPageNumber(taskPage.getNumber());
        response.setPageSize(taskPage.getSize());
        response.setTotalPages(taskPage.getTotalPages());
        response.setTotalElements(taskPage.getTotalElements());
        response.setLastPage(taskPage.isLast());
        return response;
    }

    //Ok
    @Transactional
    public TaskResponseDTO updateTask(Long customerId, String id, TaskUpdateDTO taskUpdateDTO) {
        TaskEntity taskEntity = taskRepository.findByIdAndCustomerIdAndDeletedFalse(id, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id + "" +
                        " or customer not found " + customerId));
        taskConverter.updateTask(taskEntity, taskUpdateDTO);
        taskEntity.setNotificationStatusEnum(NotificationStatusEnum.MODIFIED);
        taskEntity.setUpdatedAt(Instant.now());
        TaskEntity savedEntity = taskRepository.save(taskEntity);
        TaskEvent event = new TaskEvent(savedEntity.getId(), customerId, savedEntity.getTaskName(),
                savedEntity.getEventDate(), "MODIFIED");
        kafkaTemplate.send("task-modified-event-topic", event);
        logger.info("sending to task-modified-event-topic" + event);
        return taskConverter.toTaskResponseDTO(savedEntity);
    }


    //Atomic update - 10M scale
    @Transactional
    public TaskResponseDTO changeNotificationStatus(Long customerId, String taskId, NotificationStatusEnum status) {
        Query query = new Query(
                Criteria.where("id").is(taskId)
                        .and("customerId").is(customerId)
                        .and("deleted").is(false));
        Update update = new Update()
                .set("notificationStatusEnum", status)
                .set("updatedAt", Instant.now());
        FindAndModifyOptions options = new FindAndModifyOptions()
                .returnNew(true);
        TaskEntity updated = mongoTemplate.findAndModify(query, update, options, TaskEntity.class);
        if (updated == null) {
            throw new ResourceNotFoundException("Task not found with id " + taskId + " or not owned by customer " + customerId);
        }
        return taskConverter.toTaskResponseDTO(updated);
    }

    //Ok
    //returns all data regardless status
    public TaskResponse findAllTaskList(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort.Direction sortDirection = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Direction.ASC;
        Sort sort = Sort.by(sortDirection, sortBy);
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);
        Page<TaskEntity> taskPage = taskRepository.findAll(pageDetails);
        List<TaskEntity> taskList = taskPage.getContent();
        List<TaskResponseDTO> content = taskList.stream()
                .map(taskConverter::toTaskResponseDTO)
                .toList();
        TaskResponse response = new TaskResponse();
        response.setTasks(content);
        response.setPageNumber(taskPage.getNumber());
        response.setPageSize(taskPage.getSize());
        response.setTotalPages(taskPage.getTotalPages());
        response.setTotalElements(taskPage.getTotalElements());
        response.setLastPage(taskPage.isLast());
        return response;
    }

    //Ok
    //Returns specific active customer data
    public TaskResponse findTaskListByCustomerId(Long customerId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort.Direction sortDirection = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Direction.ASC;
        Sort sort = Sort.by(sortDirection, sortBy);
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);
        Page<TaskEntity> taskPage = taskRepository.findByCustomerIdAndDeletedFalse(customerId, pageDetails);
        List<TaskEntity> taskList = taskPage.getContent();
        List<TaskResponseDTO> content = taskList.stream()
                .map(taskConverter::toTaskResponseDTO)
                .toList();
        TaskResponse response = new TaskResponse();
        response.setTasks(content);
        response.setPageNumber(taskPage.getNumber());
        response.setPageSize(taskPage.getSize());
        response.setTotalPages(taskPage.getTotalPages());
        response.setTotalElements(taskPage.getTotalElements());
        response.setLastPage(taskPage.isLast());
        return response;
    }
}
