package com.informacolombia.prueba.presentation.rest;

import com.informacolombia.prueba.application.usecases.LoginUseCase;
import com.informacolombia.prueba.application.usecases.RegisterUseCase;
import com.informacolombia.prueba.domain.entities.Role;
import com.informacolombia.prueba.presentation.rest.dto.LoginResponse;
import com.informacolombia.prueba.presentation.rest.dto.RegisterResponse;
import com.informacolombia.prueba.presentation.rest.dto.UserInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - REST controller for authentication endpoints
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "API for user authentication and registration")
public class AuthController {
    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;

    public AuthController(
            LoginUseCase loginUseCase,
            RegisterUseCase registerUseCase
    ) {
        this.loginUseCase = loginUseCase;
        this.registerUseCase = registerUseCase;
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<LoginResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Login credentials", required = true)
            @RequestBody LoginRequest request) {
        LoginUseCase.LoginResult result = loginUseCase.execute(request.username(), request.password());
        UserInfo userInfo = new UserInfo(
                result.user().getUserId().getValue(),
                result.user().getUsername(),
                result.user().getEmail(),
                result.user().getRole().name()
        );
        LoginResponse response = new LoginResponse(result.token(), userInfo);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Registers a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<RegisterResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Registration data", required = true)
            @RequestBody RegisterRequest request) {
        Role role = request.role() != null ? Role.valueOf(request.role()) : Role.USER;
        var user = registerUseCase.execute(
                request.username(),
                request.email(),
                request.password(),
                role
        );
        RegisterResponse response = new RegisterResponse(
                user.getUserId().getValue(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
        return ResponseEntity.ok(response);
    }

    @Schema(description = "Login request")
    public record LoginRequest(
            @Schema(description = "Username", example = "john_doe", required = true)
            String username,
            @Schema(description = "Password", example = "password123", required = true)
            String password
    ) {}
    
    @Schema(description = "Registration request")
    public record RegisterRequest(
            @Schema(description = "Username", example = "john_doe", required = true)
            String username,
            @Schema(description = "Email address", example = "john@example.com", required = true)
            String email,
            @Schema(description = "Password", example = "password123", required = true)
            String password,
            @Schema(description = "User role", example = "USER", allowableValues = {"USER", "ADMIN"})
            String role
    ) {}
}
