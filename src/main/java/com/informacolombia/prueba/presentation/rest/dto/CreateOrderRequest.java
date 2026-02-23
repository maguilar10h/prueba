package com.informacolombia.prueba.presentation.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * CreateOrderRequest - Request DTO for creating an order
 */
@Schema(description = "Request to create a new order")
public record CreateOrderRequest(
        @Schema(description = "List of items in the order", required = true)
        List<OrderItemRequest> items
) {
    @Schema(description = "Order item information")
    public record OrderItemRequest(
            @Schema(description = "Product ID", example = "prod-123", required = true)
            String productId,
            @Schema(description = "Quantity of the product", example = "2", required = true)
            Integer quantity
    ) {}
}
