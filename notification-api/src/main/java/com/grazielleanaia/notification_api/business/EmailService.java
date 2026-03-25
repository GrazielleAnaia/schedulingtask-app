package com.grazielleanaia.notification_api.business;

import com.grazielleanaia.notification_api.dto.TaskEvent;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    public void sendNotification(TaskEvent taskEvent) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(taskEvent.getCustomerId().toString()); //email is better
        mailMessage.setSubject("Task Reminder");
        mailMessage.setText("You have a task scheduled at: " + taskEvent.getEventDate());
        mailSender.send(mailMessage);
    }
}
