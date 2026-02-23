package com.informacolombia.prueba.application.usecases;

import com.informacolombia.prueba.domain.entities.User;
import com.informacolombia.prueba.domain.repositories.UserRepository;
import com.informacolombia.prueba.domain.valueobjects.UserId;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * GetCurrentUserUseCase - Use case for getting current user
 */
@Service
public class GetCurrentUserUseCase {
    private final UserRepository userRepository;

    public GetCurrentUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> execute(UserId userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }
        return userRepository.findById(userId);
    }
}
