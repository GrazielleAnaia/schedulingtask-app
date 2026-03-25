package com.grazielleanaia.notification_api.business;

import com.grazielleanaia.notification_api.dto.TaskEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component

public class TaskEventConsumer {

    private final EmailService emailService;

    public TaskEventConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "task-created-topic", groupId = "notification-group")
    public void consume(TaskEvent taskEvent) {

        System.out.println("EVENT RECEIVED: " + taskEvent);
        try{
            emailService.sendNotification(taskEvent);
        } catch (Exception e){
            throw new RuntimeException("Failed to process event");
        }
    }
}
