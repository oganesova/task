package com.taskmanagement.service;

import com.taskmanagement.dto.TaskDto;
import com.taskmanagement.dto.UserDto;
import com.taskmanagement.entity.Priority;
import com.taskmanagement.entity.Status;
import com.taskmanagement.entity.Task;
import com.taskmanagement.exception.TaskNotFoundException;
import com.taskmanagement.exception.UserNotFoundException;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.taskmanagement.dto.TaskDto.mapDtoToEntity;
import static com.taskmanagement.dto.TaskDto.mapEntityToDto;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskDto createTask(TaskDto taskDto) {

        var author = userRepository.findById(taskDto.getAuthorId())
                .orElseThrow(() -> new UserNotFoundException("Author not found with ID: " + taskDto.getAuthorId()));

        var assignee = userRepository.findById(taskDto.getAssigneeId())
                .orElseThrow(() -> new UserNotFoundException("Assignee not found with ID: " + taskDto.getAssigneeId()));

        var task = mapDtoToEntity(taskDto, author, assignee);
        var savedTask = taskRepository.save(task);
        log.info("Created task with ID: {}", savedTask.getId());
        return mapEntityToDto(savedTask);
    }

    public TaskDto updateTask(Long id, TaskDto taskDto) {
        var existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        var author = userRepository.findById(taskDto.getAuthorId())
                .orElseThrow(() -> new TaskNotFoundException("Author not found"));
        var assignee = taskDto.getAssigneeId() != null ?
                userRepository.findById(taskDto.getAssigneeId())
                        .orElseThrow(() -> new TaskNotFoundException("Assignee not found")) : null;

        existingTask.setTitle(taskDto.getTitle());
        existingTask.setDescription(taskDto.getDescription());
        existingTask.setStatus(taskDto.getStatus());
        existingTask.setPriority(taskDto.getPriority());
        existingTask.setAuthor(author);
        existingTask.setAssignee(assignee);

        var updatedTask = taskRepository.save(existingTask);
        log.info("Updated task with id: {}", updatedTask.getId());
        return mapEntityToDto(updatedTask);
    }

    public void deleteTask(Long id) {
        var existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
        taskRepository.delete(existingTask);
        log.info("Deleted task with id: {}", id);
    }

    public TaskDto getTaskById(Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
        log.info("Fetched task with id: {}", id);
        return mapEntityToDto(task);
    }

    public Page<TaskDto> getTasksByStatus(Status status, Pageable pageable) {
        var tasksPage = taskRepository.findByStatus(status, pageable);
        var taskDtosPage = tasksPage.map(TaskDto::mapEntityToDto);
        log.info("Fetched {} tasks with status: {}", taskDtosPage.getTotalElements(), status);
        return taskDtosPage;
    }

    public Page<TaskDto> getTasksByPriority(Priority priority, Pageable pageable) {
        var tasksPage = taskRepository.findByPriority(priority, pageable);
        var taskDtosPage = tasksPage.map(TaskDto::mapEntityToDto);
        log.info("Fetched {} tasks with priority: {}", taskDtosPage.getTotalElements(), priority);
        return taskDtosPage;
    }

    public UserDto getAssigneeByTaskId(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id " + taskId));

        var assignee = task.getAssignee();
        if (assignee == null) {
            throw new TaskNotFoundException("Assignee not found for task with id " + taskId);
        }

        return UserDto.mapEntityToDto(assignee);
    }

    public Page<TaskDto> getTasksByAssignee(Long assigneeId, Pageable pageable) {
        Page<Task> tasksPage = taskRepository.findByAssigneeId(assigneeId, pageable);
        return tasksPage.map(TaskDto::mapEntityToDto);
    }

    public Page<TaskDto> getTasksByUser(Long userId, Pageable pageable) {
        log.info("Fetching tasks for user with id: {}", userId);
        var tasksPage = taskRepository.findByAuthorId(userId, pageable);
        var taskDtosPage = tasksPage.map(TaskDto::mapEntityToDto);

        log.info("Fetched {} tasks for user with id: {}", taskDtosPage.getTotalElements(), userId);
        return taskDtosPage;
    }

  /*  public TaskDto updateTaskStatus(Long taskId, Status status) {
        var task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id " + taskId));
        task.setStatus(status);
        var updatedTask = taskRepository.save(task);

        log.info("Task updated with ID: {}", updatedTask.getId());
        return mapEntityToDto(updatedTask);
    }*/
    public TaskDto updateTaskStatus(Long taskId, Status status) {
        var task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID " + taskId));

        task.setStatus(status);
        var updatedTask = taskRepository.save(task);

        return TaskDto.mapEntityToDto(updatedTask);
    }

    public List<TaskDto> getAllTasks() {
        log.info("Fetching all tasks");
        var tasks = taskRepository.findAll();
        var taskDtos = tasks.stream().map(TaskDto::mapEntityToDto)
                .collect(Collectors.toList());

        log.info("Fetched {} tasks", taskDtos.size());
        return taskDtos;
    }

}

