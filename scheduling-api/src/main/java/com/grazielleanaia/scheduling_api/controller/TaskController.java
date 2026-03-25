package com.grazielleanaia.scheduling_api.controller;

import com.grazielleanaia.scheduling_api.business.TaskService;
import com.grazielleanaia.scheduling_api.business.dto.TaskRequestDTO;
import com.grazielleanaia.scheduling_api.business.dto.TaskResponse;
import com.grazielleanaia.scheduling_api.business.dto.TaskResponseDTO;
import com.grazielleanaia.scheduling_api.business.dto.TaskUpdateDTO;
import com.grazielleanaia.scheduling_api.constants.AppConstants;
import com.grazielleanaia.scheduling_api.infrastructure.enums.NotificationStatusEnum;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")


public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }


    @PostMapping("/customers/{customerId}/tasks")
    public ResponseEntity<TaskResponseDTO> createTask(@RequestBody TaskRequestDTO taskRequestDTO,
                                                      @PathVariable Long customerId) throws ExecutionException, InterruptedException {
        return new ResponseEntity<>(taskService.createTask(taskRequestDTO, customerId), HttpStatus.CREATED);
    }

    //Pagination returns all data regardless status
    @GetMapping("/admin/all/tasks")
    public ResponseEntity<TaskResponse> findAllTaskList(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        return ResponseEntity.ok(taskService.findAllTaskList(pageNumber, pageSize, sortBy, sortOrder));
    }

    //Pagination returns specific active customer data
    @GetMapping("/customers/{customerId}/tasks")
    public ResponseEntity<TaskResponse> findTaskListByCustomerId(@PathVariable Long customerId,
                                                                 @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                                 @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                                 @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
                                                                 @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        return ResponseEntity.ok(taskService.findTaskListByCustomerId(customerId, pageNumber, pageSize, sortBy, sortOrder));
    }

    @GetMapping("/customers/{customerId}/pending-tasks")
    public ResponseEntity<TaskResponse> findTaskByPeriod(
            @PathVariable Long customerId,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant initialDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant finalDate) {
        return ResponseEntity.ok(taskService.findByPeriodAndPendingTask(customerId, pageNumber, pageSize, sortBy, sortOrder,
                initialDate, finalDate));
    }

    @DeleteMapping("/customers/{customerId}/tasks")
    public ResponseEntity<Void> deleteTaskById(@RequestParam("id") String id,
                                               @PathVariable Long customerId) {
        taskService.softDeleteTask(id, customerId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/customers/{customerId}/tasks")
    public ResponseEntity<TaskResponseDTO> updateTasks(@PathVariable Long customerId,
                                                       @RequestParam("id") String id,
                                                       @RequestBody TaskUpdateDTO taskUpdateDTO) {
        return new ResponseEntity<>(taskService.updateTask(customerId, id, taskUpdateDTO), HttpStatus.CREATED);
    }

    @PatchMapping("/customers/{customerId}/tasks/{taskId}/status")
    public ResponseEntity<TaskResponseDTO> changeNotificationStatus(@PathVariable Long customerId,
                                                                    @PathVariable String taskId,
                                                                    @RequestParam("status") NotificationStatusEnum status) {
        return ResponseEntity.ok(taskService.changeNotificationStatus(customerId, taskId, status));
    }
}
