package com.stms.config;

import com.stms.entity.User;
import com.stms.entity.enums.Role;
import com.stms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String @NonNull ... args) {
        if (!userRepository.existsByEmail("admin@stms.ac.tz")) {
            User admin = User.builder()
                    .registrationNumber("ADMIN-001")
                    .firstName("System")
                    .middleName(null)
                    .lastName("Administrator")
                    .email("admin@stms.ac.tz")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .firstLogin(false)
                    .build();
            userRepository.save(admin);
            log.info("Default admin created: admin@stms.ac.tz / Admin@123");
        }
    }
}
