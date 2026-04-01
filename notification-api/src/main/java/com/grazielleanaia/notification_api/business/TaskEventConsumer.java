package com.grazielleanaia.notification_api.business;

import com.grazielleanaia.notification_api.dto.TaskEvent;
import com.grazielleanaia.notification_api.error.NotRetryableException;
import com.grazielleanaia.notification_api.io.ProcessedEventEntity;
import com.grazielleanaia.notification_api.io.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;


@Component
public class TaskEventConsumer {

    private final Logger logger = LoggerFactory.getLogger(TaskEventConsumer.class);
    private final EmailService emailService;
    private final ProcessedEventRepository repository;

    public TaskEventConsumer(EmailService emailService, ProcessedEventRepository repository) {
        this.emailService = emailService;
        this.repository = repository;
    }


    @Transactional
//    @KafkaHandler
    @KafkaListener(topics = "task-created-topic", containerFactory = "kafkaListenerContainerFactory")
    public void consume(@Payload TaskEvent taskEvent,
                        @Header(value = "messageHeaderId", required = true) byte[] messageHeaderId,
                        @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {
        logger.info("Received task event {}", taskEvent.getTaskId() + " with task name " + taskEvent.getTaskName());

        //Check if this message was processed before
        String messageId = new String(messageHeaderId, StandardCharsets.UTF_8);
        ProcessedEventEntity existingRecord = repository.findByMessageId(messageId);
        if (existingRecord != null) {
            logger.info("Found existing record {}", existingRecord);
            return;
        }

        emailService.sendNotification(taskEvent);

        //Save a unique message id into DB. It should handle exception in case the same message is received
        try {
            repository.save(new ProcessedEventEntity(messageId, taskEvent.getTaskId()));
        } catch (DataIntegrityViolationException e) {
            throw new NotRetryableException(e.getMessage());
        }
    }
}
