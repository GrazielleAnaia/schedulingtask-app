package com.grazielleanaia.scheduling_api.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class TaskRequestDTO {
    @NotBlank(message = "Task name is required")
    @Size(max = 150)
    private String taskName;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Event date is required")
    private Instant eventDate;

    private Long customerId;

    public TaskRequestDTO() {
    }

    public TaskRequestDTO(String taskName, String description, Instant eventDate, Long customerId) {
        this.taskName = taskName;
        this.description = description;
        this.eventDate = eventDate;

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

}
