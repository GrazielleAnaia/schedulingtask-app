package com.grazielleanaia.notification_api.business;

import com.grazielleanaia.notification_api.dto.TaskEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TaskEventConsumer {

    private final Logger logger = LoggerFactory.getLogger(TaskEventConsumer.class);
    private final EmailService emailService;

    public TaskEventConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "task-created-topic", groupId = "notification-product-create-group")
    public void consume(TaskEvent taskEvent) {
        logger.info("Received task event {}", taskEvent.getTaskId());
        try {
            emailService.sendNotification(taskEvent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process event");
        }
    }
}
