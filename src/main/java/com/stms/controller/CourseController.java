package com.stms.controller;

import com.stms.dto.request.CourseRequest;
import com.stms.dto.response.ApiResponse;
import com.stms.dto.response.CourseResponse;
import com.stms.dto.response.UserResponse;
import com.stms.service.CourseService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Course management endpoints")
public class CourseController {

    private final CourseService courseService;

    // ── Admin only ────────────────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new course")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course created successfully", courseService.createCourse(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update course details")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Course updated", courseService.updateCourse(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a course")
    public ResponseEntity<ApiResponse<?>> deleteCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.deleteCourse(id));
    }

    @PostMapping("/{courseId}/assign-lecturer/{lecturerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign a lecturer to a course")
    public ResponseEntity<ApiResponse<?>> assignLecturer(
            @PathVariable Long courseId, @PathVariable Long lecturerId) {
        return ResponseEntity.ok(courseService.assignLecturer(courseId, lecturerId));
    }

    @DeleteMapping("/{courseId}/remove-lecturer/{lecturerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove a lecturer from a course")
    public ResponseEntity<ApiResponse<?>> removeLecturer(
            @PathVariable Long courseId, @PathVariable Long lecturerId) {
        return ResponseEntity.ok(courseService.removeLecturer(courseId, lecturerId));
    }

    // ── Shared ────────────────────────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all courses (admin) or assigned courses (lecturer/student)")
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> getAllCourses(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CourseResponse> courses = courseService.getAllCourses(search,
                PageRequest.of(page, size, Sort.by("courseCode").ascending()));
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a course by ID")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getCourseById(id)));
    }

    @GetMapping("/{id}/lecturers")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all lecturers assigned to a course")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getLecturers(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getLecturersByCourse(id)));
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('ADMIN','LECTURER')")
    @Operation(summary = "Get all students enrolled in a course")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getStudents(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getStudentsByCourse(id)));
    }

    // ── Student enrollment ────────────────────────────────────────────────────
    @PostMapping("/{courseId}/enroll/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @Operation(summary = "Enroll a student in a course")
    public ResponseEntity<ApiResponse<?>> enrollStudent(
            @PathVariable Long courseId, @PathVariable Long studentId) {
        return ResponseEntity.ok(courseService.enrollStudent(courseId, studentId));
    }

    @DeleteMapping("/{courseId}/unenroll/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @Operation(summary = "Unenroll a student from a course")
    public ResponseEntity<ApiResponse<?>> unenrollStudent(
            @PathVariable Long courseId, @PathVariable Long studentId) {
        return ResponseEntity.ok(courseService.unenrollStudent(courseId, studentId));
    }
}
