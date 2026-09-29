package com.stms.service;

import com.stms.dto.request.CreateUserRequest;
import com.stms.dto.request.UpdateUserRequest;
import com.stms.dto.response.ApiResponse;
import com.stms.dto.response.CsvImportResponse;
import com.stms.dto.response.UserResponse;
import com.stms.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse updateUser(Long id, UpdateUserRequest request);
    UserResponse getUserById(Long id);
    Page<UserResponse> getAllUsers(String search, Pageable pageable);
    Page<UserResponse> getUsersByRole(Role role, String search, Pageable pageable);
    ApiResponse<?> toggleUserStatus(Long id);
    ApiResponse<?> deleteUser(Long id);
    CsvImportResponse importStudentsFromCsv(MultipartFile file);
    byte[] generateStudentCsvTemplate();
    UserResponse getCurrentUser(String email);
}
