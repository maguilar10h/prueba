package com.informacolombia.prueba.presentation.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * RegisterResponse - Response DTO for registration operation
 */
@Schema(description = "Registration response")
public record RegisterResponse(
        @Schema(description = "User ID", example = "user-123")
        String id,
        @Schema(description = "Username", example = "john_doe")
        String username,
        @Schema(description = "Email address", example = "john@example.com")
        String email,
        @Schema(description = "User role", example = "USER")
        String role
) {}
