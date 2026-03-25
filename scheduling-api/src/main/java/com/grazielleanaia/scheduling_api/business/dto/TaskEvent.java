package com.grazielleanaia.scheduling_api.business.dto;

import java.time.Instant;

public class TaskEvent {

    private String taskId;

    private Long customerId;

    private String taskName;

    private Instant eventDate;

    private String status;

    public TaskEvent() {
    }

    public TaskEvent(String taskId, Long customerId, String taskName, Instant eventDate, String status) {
        this.taskId = taskId;
        this.customerId = customerId;
        this.taskName = taskName;
        this.eventDate = eventDate;
        this.status = status;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Instant getEventDate() {
        return eventDate;
    }

    public void setEventDate(Instant eventDate) {
        this.eventDate = eventDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
