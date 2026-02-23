package com.informacolombia.prueba.presentation.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * OrderResponse - Response DTO for order operations
 */
@Schema(description = "Order response")
public record OrderResponse(
        @Schema(description = "Order ID", example = "order-123")
        String id,
        @Schema(description = "Order status", example = "PENDING")
        String status,
        @Schema(description = "Total amount", example = "100.00")
        String totalAmount
) {}
