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
@RequestMapping("/api/v1")

public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    //purpose: current logged-in user profile create task
    //gateway rule: authenticated
    @PostMapping("/customers/me/tasks")
    public ResponseEntity<TaskResponseDTO> createTask(@RequestBody TaskRequestDTO taskRequestDTO,
                                                      @RequestHeader("X-User-Email") String email) throws ExecutionException, InterruptedException {
        return new ResponseEntity<>(taskService.createTask(taskRequestDTO, email), HttpStatus.CREATED);
    }

    @GetMapping("/admin/tasks")
    public ResponseEntity<TaskResponse> findAllTaskList(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        return ResponseEntity.ok(taskService.findAllTaskList(pageNumber, pageSize, sortBy, sortOrder));
    }


    @GetMapping(value = "/admin/customers/{customerId}/tasks", params = {"!initialDate", "!finalDate", "!status"})
    public ResponseEntity<TaskResponse> adminFindTaskListByCustomerId(@PathVariable Long customerId,
                                                                      @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                                      @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                                      @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
                                                                      @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        return ResponseEntity.ok(taskService.findAdminTaskListByCustomerId(customerId, pageNumber, pageSize, sortBy, sortOrder));
    }

    @GetMapping(value = "/customers/me/tasks", params = {"!initialDate", "!finalDate", "!status"})
    public ResponseEntity<TaskResponse> customerFindCustomerTaskList(@RequestHeader("X-User-Email") String email,
                                                                     @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                                     @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                                     @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
                                                                     @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        return ResponseEntity.ok(taskService.customerFindTaskListByCustomerEmail(email, pageNumber, pageSize, sortBy, sortOrder));
    }

    @GetMapping(value = "/customers/me/tasks", params = {"initialDate", "finalDate", "status=PENDING"})
    public ResponseEntity<TaskResponse> findTaskByPeriod(
            @RequestHeader("X-User-Email") String email,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant initialDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant finalDate,
            @RequestParam NotificationStatusEnum status) {
        return ResponseEntity.ok(taskService.findByPeriodAndPendingTask(email, pageNumber, pageSize, sortBy, sortOrder,
                initialDate, finalDate));
    }

    @DeleteMapping("/admin/customers/{customerId}/tasks/{taskId}")
    public ResponseEntity<Void> adminDeleteTaskById(@PathVariable Long customerId,
                                                    @PathVariable String taskId) {
        taskService.adminSoftDeleteTask(taskId, customerId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/customers/me/tasks/{taskId}")
    public ResponseEntity<TaskResponseDTO> updateTasks(@RequestHeader("X-User-Email") String email,
                                                       @PathVariable String taskId,
                                                       @RequestBody TaskUpdateDTO taskUpdateDTO) {
        return new ResponseEntity<>(taskService.updateTask(email, taskId, taskUpdateDTO), HttpStatus.CREATED);
    }

    @PatchMapping("/admin/customers/{customerId}/tasks/{taskId}/status")
    public ResponseEntity<TaskResponseDTO> changeNotificationStatus(@PathVariable Long customerId,
                                                                    @PathVariable String taskId,
                                                                    @RequestParam("status") NotificationStatusEnum status) {
        return ResponseEntity.ok(taskService.changeNotificationStatus(customerId, taskId, status));
    }

    @DeleteMapping("/customers/me/tasks/{taskId}")
    public ResponseEntity<Void> customerDeleteTaskById(@PathVariable String taskId,
                                                       @RequestHeader("X-User-Email") String email) {
        taskService.customerDeleteTaskById(taskId, email);
        return ResponseEntity.ok().build();
    }
}
