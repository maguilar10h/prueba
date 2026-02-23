package com.informacolombia.prueba.presentation.rest.exception;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.time.Instant;

/**
 * GlobalExceptionHandler - Centralized exception handling
 */
@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        e.getMessage(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        "Conflict",
                        e.getMessage(),
                        Instant.now()
                ));
    }


    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        "Concurrent Modification",
                        "The resource was modified by another user. Please retry.",
                        Instant.now()
                ));
    }

    @ExceptionHandler(TransientDataAccessException.class)
    public ResponseEntity<ErrorResponse> handleTransientDataAccess(TransientDataAccessException e) {
        // Check if it's a deadlock (PostgreSQL error code 40001)
        Throwable cause = e.getCause();
        if (cause instanceof SQLException sqlEx && "40001".equals(sqlEx.getSQLState())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(
                            HttpStatus.CONFLICT.value(),
                            "Deadlock Detected",
                            "A database deadlock occurred. The operation will be retried automatically.",
                            Instant.now()
                    ));
        }
        
        // Other transient errors
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        "Transient Database Error",
                        "A transient database error occurred. Please retry.",
                        Instant.now()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "An unexpected error occurred",
                        Instant.now()
                ));
    }

    public record ErrorResponse(
            int status,
            String error,
            String message,
            Instant timestamp
    ) {}
}
