package com.taskmanagement.dto;

import com.taskmanagement.entity.Comment;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

@Data
@Schema(description = "Data Transfer Object for Comment")
public class CommentDto {

    @Schema(description = "Unique identifier of the comment", example = "1")
    private Long id;

    @Schema(description = "Content of the comment", example = "This is a comment")
    private String content;

    @Schema(description = "ID of the task associated with the comment", example = "10")
    private Long taskId;

    @Schema(description = "ID of the author of the comment", example = "5")
    private Long authorId;

    public static CommentDto mapEntityToDto(Comment comment) {
        Objects.requireNonNull(comment, "Comment entity cannot be null");
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setContent(comment.getContent());
        commentDto.setTaskId(comment.getTask().getId());
        commentDto.setAuthorId(comment.getAuthor().getId());
        return commentDto;
    }

    public static Comment mapDtoToEntity(CommentDto dto, Task task, User author) {
        Objects.requireNonNull(dto, "CommentDto cannot be null");
        Comment comment = new Comment();
        comment.setId(dto.getId());
        comment.setContent(dto.getContent());
        comment.setTask(task);
        comment.setAuthor(author);
        return comment;
    }
}

