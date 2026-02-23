package com.informacolombia.prueba.infrastructure.config;

import com.informacolombia.prueba.domain.entities.Role;
import com.informacolombia.prueba.domain.entities.User;
import com.informacolombia.prueba.domain.repositories.UserRepository;
import com.informacolombia.prueba.domain.valueobjects.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * DataInitializer - Initializes test data for development/local environments
 * This component creates test users when the application starts (except in production/docker profiles)
 */
@Configuration
@Profile({"!prod", "!docker"})
public class DataInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initTestUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Create test user if it doesn't exist
            if (!userRepository.existsByUsername("testuser")) {
                String encodedPassword = passwordEncoder.encode("test123");
                User testUser = new User(
                        UserId.of("test-user-001"),
                        "testuser",
                        "test@example.com",
                        encodedPassword,
                        Role.USER
                );
                userRepository.save(testUser);
                logger.info("Created test user: testuser / test123");
            }

            // Create admin user if it doesn't exist
            if (!userRepository.existsByUsername("admin")) {
                String encodedPassword = passwordEncoder.encode("admin123");
                User adminUser = new User(
                        UserId.of("admin-user-001"),
                        "admin",
                        "admin@example.com",
                        encodedPassword,
                        Role.ADMIN
                );
                userRepository.save(adminUser);
                logger.info("Created admin user: admin / admin123");
            }
        };
    }
}
