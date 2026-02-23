package com.informacolombia.prueba.domain.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

/**
 * OrderId - Value Object representing a unique order identifier
 */
@Embeddable
public final class OrderId {
    @Column(name = "value", nullable = false)
    private String value;

    protected OrderId() {
        // For JPA
    }
    
    private OrderId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OrderId cannot be null or blank");
        }
        this.value = value;
    }

    public static OrderId of(String value) {
        return new OrderId(value);
    }

    public static OrderId generate() {
        return new OrderId(UUID.randomUUID().toString());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderId orderId = (OrderId) o;
        return Objects.equals(value, orderId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
