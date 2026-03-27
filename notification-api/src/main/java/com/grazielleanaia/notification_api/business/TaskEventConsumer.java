package com.grazielleanaia.notification_api.business;

import com.grazielleanaia.notification_api.dto.TaskEvent;
import com.grazielleanaia.notification_api.error.NotRetryableException;
import com.grazielleanaia.notification_api.error.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Service
public class TaskEventConsumer {

    private final Logger logger = LoggerFactory.getLogger(TaskEventConsumer.class);
    private final EmailService emailService;

    public TaskEventConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "task-created-topic", groupId = "notification-product-create-group")
    @KafkaHandler
    public void consume(TaskEvent taskEvent) {
        logger.info("Received task event {}", taskEvent.getTaskId());
        try {
            emailService.sendNotification(taskEvent);
        } catch (ResourceAccessException e) {
            logger.error(e.getMessage());
            throw new RetryableException("Failed to process event");
        } catch (HttpServerErrorException e) {
            logger.error(e.getMessage());
            throw new NotRetryableException("Failed to process event");
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new NotRetryableException("Failed to process event");
        }
    }
}
