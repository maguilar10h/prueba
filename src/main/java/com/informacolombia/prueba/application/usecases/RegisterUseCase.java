package com.informacolombia.prueba.application.usecases;

import com.informacolombia.prueba.domain.entities.Role;
import com.informacolombia.prueba.domain.entities.User;
import com.informacolombia.prueba.domain.repositories.UserRepository;
import com.informacolombia.prueba.domain.valueobjects.UserId;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * RegisterUseCase - Use case for user registration
 */
@Service
public class RegisterUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User execute(String username, String email, String password, Role role) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        if (role == null) {
            role = Role.USER; // Default role
        }

        if (userRepository.existsByUsername(username)) {
            throw new RegistrationException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RegistrationException("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(
                UserId.generate(),
                username,
                email,
                encodedPassword,
                role
        );

        return userRepository.save(user);
    }

    public static class RegistrationException extends RuntimeException {
        public RegistrationException(String message) {
            super(message);
        }
    }
}
