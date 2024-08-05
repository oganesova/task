package com.taskmanagement.service;
import com.taskmanagement.dto.CommentDto;
import com.taskmanagement.exception.CommentNotFoundException;
import com.taskmanagement.repository.CommentRepository;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
@Service
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, TaskRepository taskRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public CommentDto createComment(CommentDto commentDto) {
        var task = taskRepository.findById(commentDto.getTaskId())
                .orElseThrow(() -> new CommentNotFoundException("Task not found with id " + commentDto.getTaskId()));
        var author = userRepository.findById(commentDto.getAuthorId())
                .orElseThrow(() -> new CommentNotFoundException("Author not found with id " + commentDto.getAuthorId()));

        var comment = CommentDto.mapDtoToEntity(commentDto, task, author);
        var savedComment = commentRepository.save(comment);
        log.info("Created comment with id: {}", savedComment.getId());
        return CommentDto.mapEntityToDto(savedComment);
    }

    public Page<CommentDto> getCommentsByTask(Long taskId, Pageable pageable) {
        var commentsPage = commentRepository.findByTaskId(taskId, pageable);
        var commentDtosPage = commentsPage.map(CommentDto::mapEntityToDto);
        log.info("Fetched {} comments for task with id: {}", commentDtosPage.getContent().size(), taskId);
        return commentDtosPage;
    }

    public Page<CommentDto> getCommentsByUser(Long userId, Pageable pageable) {
        var commentsPage = commentRepository.findByAuthorId(userId, pageable);
        var commentDtosPage = commentsPage.map(CommentDto::mapEntityToDto);
        log.info("Fetched {} comments for user with id: {}", commentDtosPage.getContent().size(), userId);
        return commentDtosPage;
    }

    public CommentDto updateComment(Long id, CommentDto commentDto) {
        var existingComment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id " + id));

        var task = taskRepository.findById(commentDto.getTaskId())
                .orElseThrow(() -> new CommentNotFoundException("Task not found with id " + commentDto.getTaskId()));
        var author = userRepository.findById(commentDto.getAuthorId())
                .orElseThrow(() -> new CommentNotFoundException("Author not found with id " + commentDto.getAuthorId()));

        existingComment.setContent(commentDto.getContent());
        existingComment.setTask(task);
        existingComment.setAuthor(author);

        var updatedComment = commentRepository.save(existingComment);
        log.info("Updated comment with id: {}", updatedComment.getId());
        return CommentDto.mapEntityToDto(updatedComment);
    }

    public void deleteComment(Long id) {
        var existingComment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id " + id));
        commentRepository.delete(existingComment);
        log.info("Deleted comment with id: {}", id);
    }

    public CommentDto getCommentById(Long id) {
        var comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id " + id));
        log.info("Fetched comment with id: {}", id);
        return CommentDto.mapEntityToDto(comment);
    }
}