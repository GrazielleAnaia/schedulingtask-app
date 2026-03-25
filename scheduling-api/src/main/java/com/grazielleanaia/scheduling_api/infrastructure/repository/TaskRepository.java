package com.grazielleanaia.scheduling_api.infrastructure.repository;

import com.grazielleanaia.scheduling_api.infrastructure.entity.TaskEntity;
import com.grazielleanaia.scheduling_api.infrastructure.enums.NotificationStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.Optional;


public interface TaskRepository extends MongoRepository<TaskEntity, String> {

    Page<TaskEntity> findByCustomerIdAndDeletedFalse(Long customerId, Pageable pageable);

    Optional<TaskEntity> findByIdAndCustomerIdAndDeletedFalse(String id, Long customerId);

    Page<TaskEntity> findByCustomerIdAndEventDateBetweenAndNotificationStatusEnum(Long customerId, Instant initialDate, Instant finalDate,
                                                                                  NotificationStatusEnum status, Pageable pageable);
}
