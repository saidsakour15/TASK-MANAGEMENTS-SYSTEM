package com.stms.service;

import com.stms.dto.request.ChangePasswordRequest;
import com.stms.dto.request.LoginRequest;
import com.stms.dto.response.ApiResponse;
import com.stms.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    ApiResponse<?> changePassword(Long userId, ChangePasswordRequest request);
    ApiResponse<?> resetPassword(Long targetUserId);
}
