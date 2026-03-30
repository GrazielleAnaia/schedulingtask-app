package com.grazielleanaia.notification_api.business;

import com.grazielleanaia.notification_api.dto.TaskEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class TaskEventConsumer {

    private final Logger logger = LoggerFactory.getLogger(TaskEventConsumer.class);
    private final EmailService emailService;

    public TaskEventConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "task-created-topic")
    @KafkaHandler
    public void consume(@Payload TaskEvent taskEvent,
                        @Header(value = "messageHeaderId", required = true) String messageHeaderId,
                        @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {
        logger.info("Received task event {}", taskEvent.getTaskId() + " with task name" + taskEvent.getTaskName());
        emailService.sendNotification(taskEvent);
    }
}
