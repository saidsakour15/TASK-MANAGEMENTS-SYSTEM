package com.stms.controller;

import com.stms.dto.request.CommentRequest;
import com.stms.dto.request.TaskRequest;
import com.stms.dto.response.ApiResponse;
import com.stms.dto.response.CommentResponse;
import com.stms.dto.response.TaskResponse;
import com.stms.entity.User;
import com.stms.repository.UserRepository;
import com.stms.service.CommentService;
import com.stms.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

    private final TaskService taskService;
    private final CommentService commentService;
    private final UserRepository userRepository;

    private Long getUserId(Authentication auth) {
        return userRepository.findByEmail(auth.getName()).orElseThrow().getId();
    }

    // ── Lecturer ──────────────────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('LECTURER')")
    @Operation(summary = "Create a new task for an assigned course")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody TaskRequest request,
            Authentication auth) {
        Long lecturerId = getUserId(auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created", taskService.createTask(request, lecturerId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LECTURER')")
    @Operation(summary = "Update a task")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request,
            Authentication auth) {
        Long lecturerId = getUserId(auth);
        return ResponseEntity.ok(ApiResponse.success("Task updated", taskService.updateTask(id, request, lecturerId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LECTURER')")
    @Operation(summary = "Delete a task")
    public ResponseEntity<ApiResponse<?>> deleteTask(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(taskService.deleteTask(id, getUserId(auth)));
    }

    // ── Shared ────────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTaskById(id)));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get tasks for a course")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getTasksByCourse(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                taskService.getTasksByCourse(courseId, PageRequest.of(page, size, Sort.by("deadline").ascending()))));
    }

    @GetMapping("/my-tasks")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get tasks for the current user (lecturer's tasks or student's enrolled tasks)")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getMyTasks(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserId(auth);
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        Page<TaskResponse> tasks;
        if (user.getRole().name().equals("LECTURER")) {
            tasks = taskService.getTasksByLecturer(userId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        } else {
            tasks = taskService.getTasksForStudent(userId, PageRequest.of(page, size, Sort.by("deadline").ascending()));
        }
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/upcoming")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get upcoming task deadlines for current user")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getUpcomingTasks(Authentication auth) {
        Long userId = getUserId(auth);
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        List<TaskResponse> tasks;
        if (user.getRole().name().equals("LECTURER")) {
            tasks = taskService.getUpcomingTasksForLecturer(userId);
        } else {
            tasks = taskService.getUpcomingTasksForStudent(userId);
        }
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    // ── Comments ──────────────────────────────────────────────────────────────
    @PostMapping("/{taskId}/comments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add a comment to a task")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CommentRequest request,
            Authentication auth) {
        Long userId = getUserId(auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added", commentService.addComment(taskId, userId, request)));
    }

    @GetMapping("/{taskId}/comments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all comments for a task")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getComments(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                commentService.getCommentsByTask(taskId, PageRequest.of(page, size, Sort.by("commentDate").ascending()))));
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<ApiResponse<?>> deleteComment(@PathVariable Long commentId, Authentication auth) {
        return ResponseEntity.ok(commentService.deleteComment(commentId, getUserId(auth)));
    }
}
