package com.stms.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.stms.dto.request.CreateUserRequest;
import com.stms.dto.request.UpdateUserRequest;
import com.stms.dto.response.ApiResponse;
import com.stms.dto.response.CsvImportResponse;
import com.stms.dto.response.UserResponse;
import com.stms.entity.User;
import com.stms.entity.enums.Role;
import com.stms.exception.BadRequestException;
import com.stms.exception.ConflictException;
import com.stms.exception.ResourceNotFoundException;
import com.stms.repository.UserRepository;
import com.stms.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }
        if (userRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new ConflictException("Registration number already exists: " + request.getRegistrationNumber());
        }

        String defaultPassword = request.getLastName().toUpperCase();

        User user = User.builder()
                .registrationNumber(request.getRegistrationNumber())
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(defaultPassword))
                .role(request.getRole())
                .enabled(true)
                .firstLogin(true)
                .build();

        User saved = userRepository.save(user);
        log.info("User created: {} with role {}", saved.getEmail(), saved.getRole());
        return UserResponse.from(saved);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getEmail().equals(request.getEmail()) &&
            userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already in use: " + request.getEmail());
        }

        user.setFirstName(request.getFirstName());
        user.setMiddleName(request.getMiddleName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Override
    public UserResponse getUserById(Long id) {
        return UserResponse.from(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id)));
    }

    @Override
    public Page<UserResponse> getAllUsers(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return userRepository.findBySearch(search, pageable).map(UserResponse::from);
        }
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    @Override
    public Page<UserResponse> getUsersByRole(Role role, String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return userRepository.findByRoleAndSearch(role, search, pageable).map(UserResponse::from);
        }
        return userRepository.findByRole(role, pageable).map(UserResponse::from);
    }

    @Override
    @Transactional
    public ApiResponse<?> toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);
        String status = user.getEnabled() ? "activated" : "deactivated";
        return ApiResponse.success("User " + status + " successfully", null);
    }

    @Override
    @Transactional
    public ApiResponse<?> deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        return ApiResponse.success("User deleted successfully", null);
    }

    @Override
    @Transactional
    public CsvImportResponse importStudentsFromCsv(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        List<UserResponse> imported = new ArrayList<>();
        int total = 0;

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) {
                throw new BadRequestException("CSV file is empty");
            }

            // Skip header row
            rows = rows.subList(1, rows.size());
            total = rows.size();

            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);
                int lineNum = i + 2;

                try {
                    if (row.length < 5) {
                        errors.add("Line " + lineNum + ": Insufficient columns. Expected 5, got " + row.length);
                        continue;
                    }

                    String regNo = row[0].trim();
                    String firstName = row[1].trim();
                    String middleName = row[2].trim();
                    String lastName = row[3].trim();
                    String email = row[4].trim();

                    if (regNo.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                        errors.add("Line " + lineNum + ": Required fields are empty");
                        continue;
                    }

                    if (userRepository.existsByEmail(email)) {
                        errors.add("Line " + lineNum + ": Email already exists: " + email);
                        continue;
                    }
                    if (userRepository.existsByRegistrationNumber(regNo)) {
                        errors.add("Line " + lineNum + ": Registration number already exists: " + regNo);
                        continue;
                    }

                    User user = User.builder()
                            .registrationNumber(regNo)
                            .firstName(firstName)
                            .middleName(middleName.isEmpty() ? null : middleName)
                            .lastName(lastName)
                            .email(email)
                            .password(passwordEncoder.encode(lastName.toUpperCase()))
                            .role(Role.STUDENT)
                            .enabled(true)
                            .firstLogin(true)
                            .build();

                    imported.add(UserResponse.from(userRepository.save(user)));
                } catch (Exception e) {
                    errors.add("Line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (IOException | CsvException e) {
            throw new BadRequestException("Failed to parse CSV file: " + e.getMessage());
        }

        return CsvImportResponse.builder()
                .totalRecords(total)
                .successCount(imported.size())
                .failedCount(errors.size())
                .errors(errors)
                .importedUsers(imported)
                .build();
    }

    @Override
    public byte[] generateStudentCsvTemplate() {
        String csv = "registration_number,first_name,middle_name,last_name,email\n" +
                     "T21-0001-2022,John,M,Doe,john@university.ac.tz\n" +
                     "T21-0002-2022,Jane,,Smith,jane@university.ac.tz\n";
        return csv.getBytes();
    }

    @Override
    public UserResponse getCurrentUser(String email) {
        return UserResponse.from(userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }
}
