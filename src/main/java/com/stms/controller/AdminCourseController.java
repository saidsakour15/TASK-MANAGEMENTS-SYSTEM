package com.stms.controller;

import com.stms.dto.request.CourseRequest;
import com.stms.dto.response.ApiResponse;
import com.stms.dto.response.CourseResponse;
import com.stms.dto.response.CsvImportCourseResponse;
import com.stms.dto.response.UserResponse;
import com.stms.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Course Management", description = "Admin operations for course management")
public class AdminCourseController {

    private final CourseService courseService;

    @PostMapping
    @Operation(summary = "Create a new course")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.status(201).body(
                ApiResponse.success("Course created successfully", courseService.createCourse(request)));
    }

    @GetMapping
    @Operation(summary = "Get all courses with optional search")
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> getAllCourses(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "courseName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(ApiResponse.success(
                courseService.getAllCourses(search, PageRequest.of(page, size, sort))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getCourseById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a course")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully", courseService.updateCourse(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a course")
    public ResponseEntity<ApiResponse<?>> deleteCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.deleteCourse(id));
    }

    // ─── Lecturer Assignment ──────────────────────────────────────────────────

    @PostMapping("/{courseId}/lecturers/{lecturerId}")
    @Operation(summary = "Assign a lecturer to a course")
    public ResponseEntity<ApiResponse<?>> assignLecturer(
            @PathVariable Long courseId,
            @PathVariable Long lecturerId) {
        return ResponseEntity.ok(courseService.assignLecturer(courseId, lecturerId));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Bulk import course from CSV file")
    public ResponseEntity<ApiResponse<CsvImportCourseResponse>> importCourses(
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File is empty"));
        }
        return ResponseEntity.ok(ApiResponse.success("Import completed", courseService.importCoursesFromCsv(file)));
    }
    @DeleteMapping("/{courseId}/lecturers/{lecturerId}")
    @Operation(summary = "Remove a lecturer from a course")
    public ResponseEntity<ApiResponse<?>> removeLecturer(
            @PathVariable Long courseId,
            @PathVariable Long lecturerId) {
        return ResponseEntity.ok(courseService.removeLecturer(courseId, lecturerId));
    }

    @GetMapping("/{courseId}/lecturers")
    @Operation(summary = "Get all lecturers assigned to a course")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getLecturers(@PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getLecturersByCourse(courseId)));
    }

    @GetMapping("/{courseId}/students")
    @Operation(summary = "Get all students enrolled in a course")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getStudents(@PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getStudentsByCourse(courseId)));
    }
}
