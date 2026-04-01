package com.grazielleanaia.notification_api.io;

import jakarta.persistence.*;

@Entity
@Table(name = "processed-events")

public class ProcessedEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String messageId;

    @Column(nullable = false)
    private String taskID;

    public ProcessedEventEntity() {
    }

    public ProcessedEventEntity(String messageId, String taskID) {
        this.messageId = messageId;
        this.taskID = taskID;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
