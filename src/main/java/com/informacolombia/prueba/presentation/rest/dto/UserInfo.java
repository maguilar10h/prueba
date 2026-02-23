package com.informacolombia.prueba.presentation.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * UserInfo - User information in login response
 */
@Schema(description = "User information")
public record UserInfo(
        @Schema(description = "User ID", example = "user-123")
        String id,
        @Schema(description = "Username", example = "john_doe")
        String username,
        @Schema(description = "Email address", example = "john@example.com")
        String email,
        @Schema(description = "User role", example = "USER")
        String role
) {}
