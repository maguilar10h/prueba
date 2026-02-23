package com.informacolombia.prueba.domain.entities;

import com.informacolombia.prueba.domain.valueobjects.Money;
import com.informacolombia.prueba.domain.valueobjects.ProductId;
import com.informacolombia.prueba.domain.valueobjects.Quantity;
import jakarta.persistence.*;

import java.util.Objects;

/**
 * Product - Domain entity representing a product in the inventory
 */
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "product_id", unique = true, nullable = false))
    private ProductId productId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "price_amount", nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "price_currency", nullable = false))
    })
    private Money price;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "available_quantity", nullable = false))
    private Quantity availableQuantity;
    
    @Version
    private Long version;

    // Private constructor for JPA
    protected Product() {
    }

    public Product(ProductId id, String name, String description, Money price, Quantity availableQuantity) {
        if (id == null) {
            throw new IllegalArgumentException("Product id cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be null or blank");
        }
        if (price == null) {
            throw new IllegalArgumentException("Product price cannot be null");
        }
        if (availableQuantity == null) {
            throw new IllegalArgumentException("Available quantity cannot be null");
        }
        this.productId = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.availableQuantity = availableQuantity;
        this.version = 0L;
    }

    public Long getId() {
        return id;
    }
    
    public ProductId getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Money getPrice() {
        return price;
    }

    public Quantity getAvailableQuantity() {
        return availableQuantity;
    }

    public boolean hasAvailableQuantity(Quantity requestedQuantity) {
        return availableQuantity.isGreaterOrEqual(requestedQuantity);
    }

    public void reserveQuantity(Quantity quantity) {
        if (!hasAvailableQuantity(quantity)) {
            throw new IllegalStateException("Insufficient quantity available");
        }
        this.availableQuantity = this.availableQuantity.subtract(quantity);
    }

    public void releaseQuantity(Quantity quantity) {
        this.availableQuantity = this.availableQuantity.add(quantity);
    }

    public Money calculateTotal(Quantity quantity) {
        return price.multiply(java.math.BigDecimal.valueOf(quantity.getValue()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(productId, product.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
