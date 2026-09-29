package com.stms.service.impl;

import com.stms.dto.request.CommentRequest;
import com.stms.dto.response.ApiResponse;
import com.stms.dto.response.CommentResponse;
import com.stms.entity.Task;
import com.stms.entity.TaskComment;
import com.stms.entity.User;
import com.stms.entity.enums.Role;
import com.stms.exception.BadRequestException;
import com.stms.exception.ResourceNotFoundException;
import com.stms.repository.CourseEnrollmentRepository;
import com.stms.repository.CourseLecturerRepository;
import com.stms.repository.TaskCommentRepository;
import com.stms.repository.TaskRepository;
import com.stms.repository.UserRepository;
import com.stms.service.CommentService;
import com.stms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final TaskCommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseLecturerRepository courseLecturerRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public CommentResponse addComment(Long taskId, Long userId, CommentRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Access check
        Long courseId = task.getCourse().getId();
        if (user.getRole() == Role.STUDENT &&
            !enrollmentRepository.existsByCourseIdAndStudentId(courseId, userId)) {
            throw new BadRequestException("You are not enrolled in this course");
        }
        if (user.getRole() == Role.LECTURER &&
            !courseLecturerRepository.existsByCourseIdAndLecturerId(courseId, userId)) {
            throw new BadRequestException("You are not assigned to this course");
        }

        TaskComment comment = TaskComment.builder()
                .task(task)
                .user(user)
                .comment(request.getComment())
                .build();
        TaskComment saved = commentRepository.save(comment);

        // Notify lecturer if student commented
        if (user.getRole() == Role.STUDENT) {
            notificationService.createNotification(
                    task.getLecturer().getId(),
                    "New Comment on Task",
                    user.getFullName() + " commented on task: " + task.getTitle());
        }
        // Notify all students who commented if lecturer replied
        if (user.getRole() == Role.LECTURER) {
            List<Long> studentIds = commentRepository.findByTaskId(taskId, Pageable.unpaged())
                    .stream()
                    .filter(c -> c.getUser().getRole() == Role.STUDENT)
                    .map(c -> c.getUser().getId())
                    .distinct()
                    .collect(Collectors.toList());
            for (Long sid : studentIds) {
                notificationService.createNotification(sid,
                        "Lecturer replied on Task",
                        "The lecturer replied to a task you commented on: " + task.getTitle());
            }
        }

        return CommentResponse.from(saved);
    }

    @Override
    public Page<CommentResponse> getCommentsByTask(Long taskId, Pageable pageable) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found");
        }
        return commentRepository.findByTaskId(taskId, pageable).map(CommentResponse::from);
    }

    @Override
    @Transactional
    public ApiResponse<?> deleteComment(Long commentId, Long userId) {
        TaskComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        if (!comment.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only delete your own comments");
        }
        commentRepository.deleteById(commentId);
        return ApiResponse.success("Comment deleted successfully", null);
    }
}
