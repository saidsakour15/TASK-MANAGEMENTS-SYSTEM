package com.stms.service.impl;

import com.stms.dto.request.ChangePasswordRequest;
import com.stms.dto.request.LoginRequest;
import com.stms.dto.response.ApiResponse;
import com.stms.dto.response.AuthResponse;
import com.stms.entity.User;
import com.stms.exception.BadRequestException;
import com.stms.exception.ResourceNotFoundException;
import com.stms.repository.UserRepository;
import com.stms.security.JwtUtil;
import com.stms.service.AuthService;
import com.stms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BadRequestException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getEnabled()) {
            throw new BadRequestException("Account is disabled. Please contact administrator.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(userDetails, user.getId(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .userId(user.getId())
                .firstLogin(user.getFirstLogin())
                .registrationNumber(user.getRegistrationNumber())
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<?> changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setFirstLogin(false);
        userRepository.save(user);

        return ApiResponse.success("Password changed successfully", null);
    }

    @Override
    @Transactional
    public ApiResponse<?> resetPassword(Long targetUserId) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String defaultPassword = user.getLastName().toUpperCase();
        user.setPassword(passwordEncoder.encode(defaultPassword));
        user.setFirstLogin(true);
        userRepository.save(user);

        return ApiResponse.success("Password reset successfully. Default password is the user's last name in uppercase.", null);
    }
}
