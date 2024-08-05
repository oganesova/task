package com.taskmanagement.controller;

import com.taskmanagement.dto.CommentDto;
import com.taskmanagement.exception.CommentNotFoundException;
import com.taskmanagement.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "Create a new comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<Object> createComment(@RequestBody CommentDto commentDto) {
        try {
            var createdComment = commentService.createComment(commentDto);
            log.info("Comment created successfully: {}", createdComment);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
        } catch (Exception e) {
            var errorMessage = e.getMessage().contains("JSON parse error")
                    ? "Invalid JSON input: " + e.getMessage()
                    : "Failed to create comment: " + e.getMessage();
            log.error(errorMessage);
            var errorResponse = Map.of("error", errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    @Operation(summary = "Update an existing comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment updated successfully"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable Long id, @RequestBody CommentDto commentDto) {
        try {
            CommentDto updatedComment = commentService.updateComment(id, commentDto);
            log.info("Comment updated successfully: {}", updatedComment);
            return ResponseEntity.ok(updatedComment);
        } catch (Exception e) {
            log.error("Failed to update comment with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Delete a comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable Long id) {
        try {
            commentService.deleteComment(id);
            log.info("Comment deleted successfully with ID {}", id);
            return ResponseEntity.ok("Comment deleted successfully with ID " + id);
        } catch (Exception e) {
            String errorMessage = "Failed to delete comment with ID " + id + ": " + e.getMessage();
            log.error(errorMessage);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
    }


    @Operation(summary = "Get a comment by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched comment successfully"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Object> getCommentById(@PathVariable Long id) {
        try {
            var commentDto = commentService.getCommentById(id);
            log.info("Fetched comment with ID {}: {}", id, commentDto);
            return ResponseEntity.ok(commentDto);
        } catch (CommentNotFoundException e) {
            log.error("Comment with ID {} not found: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Comment not found"));
        } catch (Exception e) {
            log.error("Failed to fetch comment with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @Operation(summary = "Get comments by task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched comments successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @GetMapping("/task/{taskId}")
    public ResponseEntity<Map<String, Object>> getCommentsByTask(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var comments = commentService.getCommentsByTask(taskId, pageable);

        var response = Map.of(
                "comments", comments.isEmpty() ? "No comments found" : comments.getContent());

        log.info("Fetched comments for task with id: {}: {}", taskId, response);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get comments by user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched comments successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getCommentsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching comments for user with id: {}, page: {}, size: {}", userId, page, size);
        var pageable = PageRequest.of(page, size);
        var comments = commentService.getCommentsByUser(userId, pageable);
        var response = Map.of(
                "comments", comments.isEmpty() ? "No comments found" : comments.getContent());
        log.info("Fetched comments for user with id: {}: {}", userId, comments.getContent());
        return ResponseEntity.ok(response);
    }
}

