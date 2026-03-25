package com.grazielleanaia.scheduling_api.business.dto;

import com.grazielleanaia.scheduling_api.infrastructure.enums.NotificationStatusEnum;

import java.time.Instant;

public class TaskResponseDTO {

    private String id;

    private String taskName;

    private String description;

    private Instant eventDate;

    //Safe to expose createdAt & updatedAt for frontend sorting, debugging
    private Instant createdAt;

    private Instant updatedAt;

    private NotificationStatusEnum notificationStatusEnum;

    private Long customerId;

    public TaskResponseDTO() {
    }

    public TaskResponseDTO(String id, String taskName, String description, Instant eventDate, Instant createdAt,
                           Instant updatedAt, NotificationStatusEnum notificationStatusEnum, Long customerId) {
        this.id = id;
        this.taskName = taskName;
        this.description = description;
        this.eventDate = eventDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.notificationStatusEnum = notificationStatusEnum;
        this.customerId = customerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
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
