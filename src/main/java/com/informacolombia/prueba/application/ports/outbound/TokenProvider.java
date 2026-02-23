package com.informacolombia.prueba.application.ports.outbound;

import com.informacolombia.prueba.domain.valueobjects.UserId;

/**
 * TokenProvider - Output port for JWT token operations
 */
public interface TokenProvider {
    /**
     * Generates a JWT token for a user
     */
    String generateToken(UserId userId, String username, String role);

    /**
     * Validates a JWT token
     */
    boolean validateToken(String token);

    /**
     * Extracts user ID from token
     */
    UserId getUserIdFromToken(String token);

    /**
     * Extracts username from token
     */
    String getUsernameFromToken(String token);

    /**
     * Extracts role from token
     */
    String getRoleFromToken(String token);
}
