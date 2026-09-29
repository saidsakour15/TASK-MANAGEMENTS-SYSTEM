package com.stms.controller;

import com.stms.dto.request.CommentRequest;
import com.stms.dto.request.TaskRequest;
import com.stms.dto.response.*;
import com.stms.entity.User;
import com.stms.repository.UserRepository;
import com.stms.service.CommentService;
import com.stms.service.CourseService;
import com.stms.service.NotificationService;
import com.stms.service.TaskService;
import com.stms.service.impl.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lecturer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LECTURER')")
@Tag(name = "Lecturer", description = "Lecturer operations")
public class    LecturerController {

    private final CourseService courseService;
    private final TaskService taskService;
    private final CommentService commentService;
    private final NotificationService notificationService;
    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    private User resolveUser(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername()).orElseThrow();
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get lecturer dashboard statistics")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboard(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getLecturerStats(resolveUser(ud).getId())));
    }

    @GetMapping("/courses")
    @Operation(summary = "Get courses assigned to this lecturer")
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> getMyCourses(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                courseService.getCoursesByLecturer(resolveUser(ud).getId(), PageRequest.of(page, size))));
    }

    @GetMapping("/courses/{courseId}/students")
    @Operation(summary = "Get enrolled students for a course")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getCourseStudents(@PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getStudentsByCourse(courseId)));
    }

    @PostMapping("/tasks")
    @Operation(summary = "Create a task for an assigned course")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(201).body(
                ApiResponse.success("Task created", taskService.createTask(request, resolveUser(ud).getId())));
    }

    @GetMapping("/tasks")
    @Operation(summary = "Get all tasks by this lecturer")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getMyTasks(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(ApiResponse.success(
                taskService.getTasksByLecturer(resolveUser(ud).getId(), PageRequest.of(page, size, sort))));
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTaskById(id)));
    }

    @PutMapping("/tasks/{id}")
    @Operation(summary = "Update a task")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task updated",
                taskService.updateTask(id, request, resolveUser(ud).getId())));
    }

    @DeleteMapping("/tasks/{id}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<ApiResponse<?>> deleteTask(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long id) {
        return ResponseEntity.ok(taskService.deleteTask(id, resolveUser(ud).getId()));
    }

    @GetMapping("/tasks/upcoming")
    @Operation(summary = "Get upcoming task deadlines")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getUpcomingTasks(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.success(
                taskService.getUpcomingTasksForLecturer(resolveUser(ud).getId())));
    }

    @PostMapping("/tasks/{taskId}/comments")
    @Operation(summary = "Comment on a task")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long taskId,
            @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.status(201).body(
                ApiResponse.success(commentService.addComment(taskId, resolveUser(ud).getId(), request)));
    }

    @GetMapping("/tasks/{taskId}/comments")
    @Operation(summary = "Get comments for a task")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getComments(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                commentService.getCommentsByTask(taskId, PageRequest.of(page, size))));
    }

    @GetMapping("/notifications")
    @Operation(summary = "Get notifications")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getNotifications(resolveUser(ud).getId(), PageRequest.of(page, size))));
    }

    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<ApiResponse<?>> markRead(@AuthenticationPrincipal UserDetails ud, @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id, resolveUser(ud).getId()));
    }

    @PatchMapping("/notifications/read-all")
    public ResponseEntity<ApiResponse<?>> markAllRead(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(notificationService.markAllAsRead(resolveUser(ud).getId()));
    }
}
