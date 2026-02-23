package com.informacolombia.prueba.presentation.rest.dto;

/**
 * OrderItemDTO - Data Transfer Object for OrderItem
 */
public record OrderItemDTO(
        String productId,
        Integer quantity,
        String unitPrice,
        String totalPrice,
        String currency
) {}
