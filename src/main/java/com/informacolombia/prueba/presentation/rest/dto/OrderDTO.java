package com.informacolombia.prueba.presentation.rest.dto;

import com.informacolombia.prueba.domain.valueobjects.OrderStatus;

import java.time.Instant;
import java.util.List;

/**
 * OrderDTO - Data Transfer Object for Order
 */
public record OrderDTO(
        String id,
        String userId,
        List<OrderItemDTO> items,
        OrderStatus status,
        String totalAmount,
        String currency,
        Instant createdAt,
        Instant updatedAt
) {}
