package com.informacolombia.prueba.domain.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

/**
 * ProductId - Value Object representing a unique product identifier
 */
@Embeddable
public final class ProductId {
    @Column(name = "value", nullable = false)
    private String value;

    protected ProductId() {
        // For JPA
    }
    
    private ProductId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ProductId cannot be null or blank");
        }
        this.value = value;
    }

    public static ProductId of(String value) {
        return new ProductId(value);
    }

    public static ProductId generate() {
        return new ProductId(UUID.randomUUID().toString());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductId productId = (ProductId) o;
        return Objects.equals(value, productId.value);
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
