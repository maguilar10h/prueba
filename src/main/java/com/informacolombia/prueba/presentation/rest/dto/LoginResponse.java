package com.informacolombia.prueba.presentation.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * LoginResponse - Response DTO for login operation
 */
@Schema(description = "Login response")
public record LoginResponse(
        @Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,
        @Schema(description = "User information")
        UserInfo user
) {}
