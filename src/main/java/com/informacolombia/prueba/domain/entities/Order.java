package com.informacolombia.prueba.domain.entities;

import com.informacolombia.prueba.domain.events.OrderCreatedEvent;
import com.informacolombia.prueba.domain.events.OrderStatusChangedEvent;
import com.informacolombia.prueba.domain.services.OrderStateMachine;
import com.informacolombia.prueba.domain.valueobjects.Money;
import com.informacolombia.prueba.domain.valueobjects.OrderId;
import com.informacolombia.prueba.domain.valueobjects.OrderStatus;
import com.informacolombia.prueba.domain.valueobjects.UserId;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Order - Domain entity representing an order in the system
 */
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "order_id", unique = true, nullable = false))
    private OrderId orderId;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "user_id", nullable = false))
    private UserId userId;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    private List<OrderItem> items;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "total_amount", nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "currency", nullable = false))
    })
    private Money totalAmount;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    @Column(nullable = false)
    private Instant updatedAt;
    
    @Version
    private Long version;

    @Transient
    private final List<Object> domainEvents = new ArrayList<>();

    protected Order() {
        this.items = new ArrayList<>();
    }

    public Order(OrderId id, UserId userId, List<OrderItem> items) {
        if (id == null) {
            throw new IllegalArgumentException("Order id cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        this.orderId = id;
        this.userId = userId;
        this.items = new ArrayList<>(items);
        this.items.forEach(item -> item.setOrderId(id.getValue()));
        this.status = OrderStatus.PENDING;
        this.totalAmount = calculateTotal();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.version = 0L;

        addDomainEvent(new OrderCreatedEvent(id, userId));
    }

    public Long getId() {
        return id;
    }
    
    public OrderId getOrderId() {
        return orderId;
    }

    public UserId getUserId() {
        return userId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    /**
     * Changes the order status if the transition is valid
     */
    public void changeStatus(OrderStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        if (!OrderStateMachine.isValidTransition(this.status, newStatus)) {
            throw new IllegalStateException(
                    String.format("Invalid transition from %s to %s", this.status, newStatus)
            );
        }

        OrderStatus oldStatus = this.status;
        this.status = newStatus;
        this.updatedAt = Instant.now();

        addDomainEvent(new OrderStatusChangedEvent(orderId, oldStatus, newStatus));
    }

    /**
     * Confirms the order
     */
    public void confirm() {
        changeStatus(OrderStatus.CONFIRMED);
    }

    /**
     * Marks the order as processing
     */
    public void startProcessing() {
        changeStatus(OrderStatus.PROCESSING);
    }

    /**
     * Marks the order as shipped
     */
    public void ship() {
        changeStatus(OrderStatus.SHIPPED);
    }

    /**
     * Marks the order as delivered
     */
    public void deliver() {
        changeStatus(OrderStatus.DELIVERED);
    }

    /**
     * Cancels the order
     */
    public void cancel() {
        if (OrderStateMachine.isFinalState(this.status)) {
            throw new IllegalStateException("Cannot cancel order in final state: " + this.status);
        }
        changeStatus(OrderStatus.CANCELLED);
    }

    /**
     * Marks the order as failed
     */
    public void markAsFailed() {
        changeStatus(OrderStatus.FAILED);
    }

    /**
     * Checks if the order can be modified
     */
    public boolean canBeModified() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    /**
     * Checks if the order is in a final state
     */
    public boolean isInFinalState() {
        return OrderStateMachine.isFinalState(status);
    }

    private Money calculateTotal() {
        return items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(Money.zero("USD"), Money::add);
    }

    private void addDomainEvent(Object event) {
        domainEvents.add(event);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
