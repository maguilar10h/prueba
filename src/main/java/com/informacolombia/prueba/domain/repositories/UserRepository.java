package com.informacolombia.prueba.domain.repositories;

import com.informacolombia.prueba.domain.entities.User;
import com.informacolombia.prueba.domain.valueobjects.UserId;

import java.util.Optional;

/**
 * UserRepository - Output port for user persistence
 */
public interface UserRepository {
    /**
     * Saves a user
     */
    User save(User user);

    /**
     * Finds a user by ID
     */
    Optional<User> findById(UserId userId);

    /**
     * Finds a user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a username exists
     */
    boolean existsByUsername(String username);

    /**
     * Checks if an email exists
     */
    boolean existsByEmail(String email);
}
