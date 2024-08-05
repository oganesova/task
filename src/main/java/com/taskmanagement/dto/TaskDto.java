package com.taskmanagement.dto;

import com.taskmanagement.entity.Priority;
import com.taskmanagement.entity.Status;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object for Task")
public class TaskDto {
    @Schema(description = "Unique identifier of the task", example = "1")
    private Long id;
    @Schema(description = "Title of the task", example = "Implement feature XYZ")
    private String title;
    @Schema(description = "Description of the task", example = "Details about the feature to be implemented")
    private String description;
    @Schema(description = "Priority of the task", example = "HIGH")
    private Priority priority;
    @Schema(description = "Status of the task", example = "IN_PROGRESS")
    private Status status;
    @Schema(description = "ID of the author of the task", example = "5")
    private Long authorId;
    @Schema(description = "ID of the assignee of the task", example = "10")
    private Long assigneeId;

    public static Task mapDtoToEntity(TaskDto taskDto, User author, User assignee) {
        Task task = new Task();
        task.setId(taskDto.getId());
        task.setTitle(taskDto.getTitle());
        task.setDescription(taskDto.getDescription());
        task.setPriority(taskDto.getPriority());
        task.setStatus(taskDto.getStatus());
        task.setAuthor(author);
        task.setAssignee(assignee);
        return task;
    }

    public static TaskDto mapEntityToDto(Task task) {
        TaskDto taskDto = new TaskDto();
        taskDto.setId(task.getId());
        taskDto.setTitle(task.getTitle());
        taskDto.setDescription(task.getDescription());
        taskDto.setPriority(task.getPriority());
        taskDto.setStatus(task.getStatus());
        taskDto.setAuthorId(task.getAuthor().getId());
        taskDto.setAssigneeId(task.getAssignee().getId());
        return taskDto;
    }
}
