package com.stms.controller;

import com.stms.dto.request.CommentRequest;
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
@RequestMapping("/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
@Tag(name = "Student", description = "Student operations")
public class StudentController {

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
    @Operation(summary = "Student dashboard stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboard(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getStudentStats(resolveUser(ud).getId())));
    }

    @GetMapping("/courses/available")
    @Operation(summary = "Browse available courses")
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> getAvailableCourses(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                courseService.getAvailableCourses(resolveUser(ud).getId(), PageRequest.of(page, size))));
    }

    @GetMapping("/courses/enrolled")
    @Operation(summary = "Get enrolled courses")
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> getEnrolledCourses(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                courseService.getEnrolledCoursesByStudent(resolveUser(ud).getId(), PageRequest.of(page, size))));
    }

    @PostMapping("/courses/{courseId}/enroll")
    @Operation(summary = "Enroll in a course")
    public ResponseEntity<ApiResponse<?>> enroll(@AuthenticationPrincipal UserDetails ud, @PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.enrollStudent(courseId, resolveUser(ud).getId()));
    }

    @DeleteMapping("/courses/{courseId}/unenroll")
    @Operation(summary = "Unenroll from a course")
    public ResponseEntity<ApiResponse<?>> unenroll(@AuthenticationPrincipal UserDetails ud, @PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.unenrollStudent(courseId, resolveUser(ud).getId()));
    }

    @GetMapping("/tasks")
    @Operation(summary = "Get tasks from enrolled courses")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getMyTasks(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "deadline") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(ApiResponse.success(
                taskService.getTasksForStudent(resolveUser(ud).getId(), PageRequest.of(page, size, sort))));
    }

    @GetMapping("/tasks/{id}")
    @Operation(summary = "Get task detail")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTaskById(id)));
    }

    @GetMapping("/tasks/upcoming")
    @Operation(summary = "Upcoming task deadlines")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getUpcoming(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.success(
                taskService.getUpcomingTasksForStudent(resolveUser(ud).getId())));
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
    @Operation(summary = "View task comments")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getComments(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                commentService.getCommentsByTask(taskId, PageRequest.of(page, size))));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Delete own comment")
    public ResponseEntity<ApiResponse<?>> deleteComment(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.deleteComment(commentId, resolveUser(ud).getId()));
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
