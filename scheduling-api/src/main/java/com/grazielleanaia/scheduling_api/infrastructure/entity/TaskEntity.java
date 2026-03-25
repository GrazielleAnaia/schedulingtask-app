package com.grazielleanaia.scheduling_api.infrastructure.entity;


import com.grazielleanaia.scheduling_api.infrastructure.enums.NotificationStatusEnum;
import jakarta.validation.constraints.Future;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

//Maps to a MongoDB collection named task
//Each instance (one document in the collection) equivalent to db.task
@Document("tasks")
@CompoundIndex(name = "customer_event_idx", def = "{'customerEmail': 1, 'eventDate': 1, 'deleted': 1}")
@CompoundIndex(name = "event_status_deleted_idx", def = "{'notificationStatusEnum': 1, 'eventDate': 1, 'deleted': 1}")
@CompoundIndex(name = "id_customer_deleted_idx", def = "{'id': 1, 'customerEmail': 1, 'deleted': 1}")
public class TaskEntity {

    //MongoDB indexes it by default
    @Id
    private String id;

    //Creates a DB index like db.task.createIndex({ customerEmail: 1 })
    //Mongo uses index O(log n)
    //Without index -> full collection scan (slow for millions of docs)
    @Indexed
    private String customerEmail;

    @Indexed
    private String taskName;

    private String description;

    //Task scheduled for future, reminder system, notification trigger, event calendar
    @Indexed
    @Future(message = "event date must be in the future")
    private Instant eventDate;

    //Auditing field for tracking changes, sync logic, debugging
    @CreatedDate
    private Instant createdAt;

    //Auditing field for tracking changes, sync logic, debugging
    @LastModifiedDate
    private Instant updatedAt;

    //Notification life cycle
    @Indexed
    private NotificationStatusEnum notificationStatusEnum;

    private boolean deleted = false;

    @Version
    private Long version;

    private Long customerId;

    //without index -> full scan
    //with index -> efficient range query
    public TaskEntity() {
    }

    public TaskEntity(String id, String customerEmail, String taskName, String description, Instant eventDate, Instant createdAt, Instant updatedAt,
                      NotificationStatusEnum notificationStatusEnum, boolean deleted, Long version, Long customerId) {
        this.id = id;
        this.customerEmail = customerEmail;
        this.taskName = taskName;
        this.description = description;
        this.eventDate = eventDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.notificationStatusEnum = notificationStatusEnum;
        this.deleted = deleted;
        this.version = version;
        this.customerId = customerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}

