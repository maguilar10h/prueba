package com.informacolombia.prueba.domain.entities;

import com.informacolombia.prueba.domain.valueobjects.OrderId;
import com.informacolombia.prueba.domain.valueobjects.ProductId;
import com.informacolombia.prueba.domain.valueobjects.Quantity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

/**
 * Inventory - Domain entity representing an inventory reservation
 */
@Entity
@Table(name = "inventory")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "order_id", nullable = false))
    private OrderId orderId;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "product_id", nullable = false))
    private ProductId productId;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "reserved_quantity", nullable = false))
    private Quantity reservedQuantity;
    
    @Column(nullable = false)
    private Instant reservedAt;
    
    @Column(nullable = false)
    private boolean released;
    
    private Instant releasedAt;
    
    @Version
    private Long version; // For optimistic locking

    // Private constructor for JPA
    protected Inventory() {
    }

    public Inventory(OrderId orderId, ProductId productId, Quantity reservedQuantity) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order id cannot be null");
        }
        if (productId == null) {
            throw new IllegalArgumentException("Product id cannot be null");
        }
        if (reservedQuantity == null) {
            throw new IllegalArgumentException("Reserved quantity cannot be null");
        }
        this.orderId = orderId;
        this.productId = productId;
        this.reservedQuantity = reservedQuantity;
        this.reservedAt = Instant.now();
        this.released = false;
        this.version = 0L;
    }

    public Long getId() {
        return id;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public ProductId getProductId() {
        return productId;
    }

    public Quantity getReservedQuantity() {
        return reservedQuantity;
    }

    public Instant getReservedAt() {
        return reservedAt;
    }

    public boolean isReleased() {
        return released;
    }

    public Instant getReleasedAt() {
        return releasedAt;
    }

    public Long getVersion() {
        return version;
    }

    /**
     * Releases the inventory reservation
     */
    public void release() {
        if (released) {
            throw new IllegalStateException("Inventory already released");
        }
        this.released = true;
        this.releasedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inventory inventory = (Inventory) o;
        return Objects.equals(id, inventory.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
