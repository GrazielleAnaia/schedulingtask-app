package com.grazielleanaia.scheduling_api.business.dto;

import com.grazielleanaia.scheduling_api.infrastructure.enums.NotificationStatusEnum;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class TaskUpdateDTO {

    @Size(max = 150)
    private String taskName;

    @Size(max = 1000)
    private String description;

    private Instant eventDate;

    private NotificationStatusEnum notificationStatusEnum;

    private Long customerId;

    public TaskUpdateDTO() {
    }

    public TaskUpdateDTO(String taskName, String description, Instant eventDate,
                         NotificationStatusEnum notificationStatusEnum, Long customerId) {
        this.taskName = taskName;
        this.description = description;
        this.eventDate = eventDate;
        this.notificationStatusEnum = notificationStatusEnum;
        this.customerId = customerId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getEventDate() {
        return eventDate;
    }

    public void setEventDate(Instant eventDate) {
        this.eventDate = eventDate;
    }

    public NotificationStatusEnum getNotificationStatusEnum() {
        return notificationStatusEnum;
    }

    public void setNotificationStatusEnum(NotificationStatusEnum notificationStatusEnum) {
        this.notificationStatusEnum = notificationStatusEnum;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}
