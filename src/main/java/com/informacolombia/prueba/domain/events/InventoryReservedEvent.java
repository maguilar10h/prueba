package com.informacolombia.prueba.domain.events;

import com.informacolombia.prueba.domain.valueobjects.OrderId;
import com.informacolombia.prueba.domain.valueobjects.ProductId;
import com.informacolombia.prueba.domain.valueobjects.Quantity;

import java.time.Instant;

/**
 * InventoryReservedEvent - Domain event fired when inventory is reserved
 */
public record InventoryReservedEvent(
        OrderId orderId,
        ProductId productId,
        Quantity quantity,
        Instant occurredOn
) implements DomainEvent {

    public InventoryReservedEvent(OrderId orderId, ProductId productId, Quantity quantity) {
        this(orderId, productId, quantity, Instant.now());
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    @Override
    public String eventType() {
        return "InventoryReserved";
    }
}
