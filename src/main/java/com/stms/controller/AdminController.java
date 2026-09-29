package com.stms.controller;

import com.stms.dto.request.CreateUserRequest;
import com.stms.dto.request.UpdateUserRequest;
import com.stms.dto.response.ApiResponse;
import com.stms.dto.response.CsvImportResponse;
import com.stms.dto.response.DashboardStatsResponse;
import com.stms.dto.response.UserResponse;
import com.stms.entity.enums.Role;
import com.stms.service.AuthService;
import com.stms.service.UserService;
import com.stms.service.impl.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrator endpoints")
public class AdminController {

    private final UserService userService;
    private final AuthService authService;
    private final DashboardService dashboardService;

    // ── Dashboard ────────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard statistics")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getAdminStats()));
    }

    // ── Users ────────────────────────────────────────────────────────────────
    @PostMapping("/users")
    @Operation(summary = "Create a new user account")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", userService.createUser(request)));
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users with optional search and pagination")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<UserResponse> users = userService.getAllUsers(search, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/users/role/{role}")
    @Operation(summary = "Get users by role")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsersByRole(
            @PathVariable Role role,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserResponse> users = userService.getUsersByRole(role, search,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Update user information")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", userService.updateUser(id, request)));
    }

    @PatchMapping("/users/{id}/toggle-status")
    @Operation(summary = "Activate or deactivate a user")
    public ResponseEntity<ApiResponse<?>> toggleUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleUserStatus(id));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete a user")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    @PostMapping("/users/{id}/reset-password")
    @Operation(summary = "Reset user password to default (LASTNAME)")
    public ResponseEntity<ApiResponse<?>> resetPassword(@PathVariable Long id) {
        return ResponseEntity.ok(authService.resetPassword(id));
    }

    // ── CSV Bulk Import ───────────────────────────────────────────────────────
    @PostMapping(value = "/users/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Bulk import students from CSV file")
    public ResponseEntity<ApiResponse<CsvImportResponse>> importStudents(
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File is empty"));
        }
        return ResponseEntity.ok(ApiResponse.success("Import completed", userService.importStudentsFromCsv(file)));
    }

    @GetMapping("/users/import/template")
    @Operation(summary = "Download sample CSV template for student import")
    public ResponseEntity<byte[]> downloadCsvTemplate() {
        byte[] template = userService.generateStudentCsvTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "students_template.csv");
        return ResponseEntity.ok().headers(headers).body(template);
    }
}
