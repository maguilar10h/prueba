package com.informacolombia.prueba.domain.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/**
 * Quantity - Value Object representing a quantity amount
 */
@Embeddable
public final class Quantity {
    @Column(name = "value", nullable = false)
    private int value;

    protected Quantity() {
        // For JPA
    }
    
    private Quantity(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.value = value;
    }

    public static Quantity of(int value) {
        return new Quantity(value);
    }

    public static Quantity zero() {
        return new Quantity(0);
    }

    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }

    public Quantity subtract(Quantity other) {
        int result = this.value - other.value;
        if (result < 0) {
            throw new IllegalArgumentException("Result cannot be negative");
        }
        return new Quantity(result);
    }

    public boolean isGreaterThan(Quantity other) {
        return this.value > other.value;
    }

    public boolean isLessThan(Quantity other) {
        return this.value < other.value;
    }

    public boolean isGreaterOrEqual(Quantity other) {
        return this.value >= other.value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quantity quantity = (Quantity) o;
        return value == quantity.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
