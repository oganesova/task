package com.taskmanagement.controller;

import com.taskmanagement.dto.TaskDto;
import com.taskmanagement.entity.Priority;
import com.taskmanagement.entity.Status;
import com.taskmanagement.exception.TaskNotFoundException;
import com.taskmanagement.exception.UserNotFoundException;
import com.taskmanagement.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Create a new task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskDto taskDto) {
        try {
            TaskDto createdTask = taskService.createTask(taskDto);
            log.info("Task created successfully with ID: {}", createdTask.getId());
            return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
        } catch (UserNotFoundException e) {
            log.error("Error creating task: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Unexpected error creating task", e);
            return new ResponseEntity<>("Unexpected error creating task", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Update an existing task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<String> updateTask(@PathVariable Long id, @RequestBody TaskDto taskDto) {
        try {
            TaskDto updatedTask = taskService.updateTask(id, taskDto);
            log.info("Task updated successfully: {}", updatedTask);
            return ResponseEntity.ok("Task updated successfully with ID: " + updatedTask.getId());
        } catch (TaskNotFoundException e) {
            log.error("Task not found with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found with ID: " + id);
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to update task with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update task due to an unexpected error.");
        }
    }

    @Operation(summary = "Delete a task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            log.info("Task deleted successfully with ID {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete task with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Get a task by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched task successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Object> getTaskById(@PathVariable Long id) {
        try {
            var taskDto = taskService.getTaskById(id);
            log.info("Fetched task with ID {}: {}", id, taskDto);
            return ResponseEntity.ok(taskDto);
        } catch (TaskNotFoundException e) {
            log.error("Task with ID {} not found: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Task not found"));
        } catch (Exception e) {
            log.error("Failed to fetch task with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @Operation(summary = "Get tasks by assignee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched tasks successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @GetMapping("/assignee/{assigneeId}")
    public ResponseEntity<?> getTasksByAssignee(
            @PathVariable Long assigneeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching tasks for assignee with id: {}, page: {}, size: {}", assigneeId, page, size);
        try {
            var pageable = PageRequest.of(page, size);
            var tasks = taskService.getTasksByAssignee(assigneeId, pageable);
            log.info("Fetched {} tasks for assignee with id: {}", tasks.getTotalElements(), assigneeId);
            return ResponseEntity.ok(tasks);
        } catch (TaskNotFoundException e) {
            log.error("Task with AssigneeID {} not found: {}", assigneeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Task not found"));
        } catch (Exception e) {
            log.error("Failed to fetch task with AssigneeID {}: {}", assigneeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }

    }

    @Operation(summary = "Get the assignee of a task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignee fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Task or assignee not found")
    })
    @GetMapping("/{taskId}/assignee")
    public ResponseEntity<?> getAssigneeByTaskId(@PathVariable Long taskId) {
        try {
            var assignee = taskService.getAssigneeByTaskId(taskId);
            log.info("Fetched assignee for task with id: {}", taskId);
            return ResponseEntity.ok(assignee);
        } catch (TaskNotFoundException e) {
            log.error("Assignee with TaskID {} not found: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Task not found"));
        } catch (Exception e) {
            log.error("Failed to fetch assignee with TaskID {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @Operation(summary = "Get tasks by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched tasks successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status value")
    })
    @GetMapping("/status")
    public ResponseEntity<?> getTasksByStatus(
            @RequestParam Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Fetching tasks with status: {}, page: {}, size: {}", status, page, size);
            var pageable = PageRequest.of(page, size);
            var tasks = taskService.getTasksByStatus(status, pageable);
            log.info("Fetched {} tasks with status: {}", tasks.getTotalElements(), status);
            return ResponseEntity.ok(tasks);
        } catch (TaskNotFoundException e) {
            log.error("Tasks with status {} not found: {}", status, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Tasks not found with status: " + status));
        } catch (Exception e) {
            log.error("Failed to fetch tasks with status {}: {}", status, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @Operation(summary = "Get tasks by priority")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched tasks successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid priority value")
    })
    @GetMapping("/priority")
    public ResponseEntity<?> getTasksByPriority(
            @RequestParam Priority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Fetching tasks with priority: {}, page: {}, size: {}", priority, page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<TaskDto> tasks = taskService.getTasksByPriority(priority, pageable);
            log.info("Fetched {} tasks with priority: {}", tasks.getTotalElements(), priority);
            return ResponseEntity.ok(tasks);
        } catch (TaskNotFoundException e) {
            log.error("Tasks with priority {} not found: {}", priority, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Tasks not found with status: " + priority));
        } catch (Exception e) {
            log.error("Failed to fetch tasks with priority {}: {}", priority, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }

    }

    @Operation(summary = "Get tasks by user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched tasks successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getTasksByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching tasks for user with id: {}, page: {}, size: {}", userId, page, size);
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<TaskDto> tasksPage = taskService.getTasksByUser(userId, pageable);

            if (tasksPage.getTotalElements() == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No tasks found for user with ID: " + userId));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("content", tasksPage.getContent());

            log.info("Fetched {} tasks for user with id: {}", tasksPage.getTotalElements(), userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch tasks for user with ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @Operation(summary = "Update task status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam Status status) {
        try {
            var updatedTask = taskService.updateTaskStatus(taskId, status);
            log.info("Updated status for task with ID: {}", updatedTask.getId());
            return ResponseEntity.ok(updatedTask);
        } catch (IllegalArgumentException e) {
            log.error("Invalid status value provided: {}", status);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid status provided"));
        } catch (Exception e) {
            log.error("Failed to update status for task with ID {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Failed to update status for task with this ID"));
        }

    }

    @Operation(summary = "Get all tasks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched all tasks successfully")
    })
    @GetMapping
    public ResponseEntity<List<TaskDto>> getAllTasks() {
        log.info("Fetching all tasks");
        List<TaskDto> taskDtos = taskService.getAllTasks();
        log.info("Fetched {} tasks", taskDtos.size());
        return ResponseEntity.ok(taskDtos);

    }

}

