package com.informacolombia.prueba.domain.valueobjects;

/**
 * Order Status - Enum representing the possible states of an order
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    FAILED
}
