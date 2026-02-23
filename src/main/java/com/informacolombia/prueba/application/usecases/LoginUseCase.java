package com.informacolombia.prueba.application.usecases;

import com.informacolombia.prueba.application.ports.outbound.TokenProvider;
import com.informacolombia.prueba.domain.entities.User;
import com.informacolombia.prueba.domain.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * LoginUseCase - Use case for user authentication
 */
@Service
public class LoginUseCase {
    private static final Logger logger = LoggerFactory.getLogger(LoginUseCase.class);
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public LoginUseCase(UserRepository userRepository, TokenProvider tokenProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResult execute(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            logger.warn("Login attempt failed: User '{}' not found", username);
            throw new AuthenticationException("Invalid username or password");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Login attempt failed: Invalid password for user '{}'", username);
            throw new AuthenticationException("Invalid username or password");
        }

        logger.info("Successful login for user '{}'", username);

        String token = tokenProvider.generateToken(
                user.getUserId(),
                user.getUsername(),
                user.getRole().name()
        );

        return new LoginResult(token, user);
    }

    public record LoginResult(String token, User user) {
    }

    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
    }
}
