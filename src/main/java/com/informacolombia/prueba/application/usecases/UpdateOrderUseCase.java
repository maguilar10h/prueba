package com.informacolombia.prueba.application.usecases;

import com.informacolombia.prueba.application.ports.outbound.EventPublisher;
import com.informacolombia.prueba.domain.entities.Order;
import com.informacolombia.prueba.domain.repositories.OrderRepository;
import com.informacolombia.prueba.domain.valueobjects.OrderId;
import jakarta.persistence.OptimisticLockException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UpdateOrderUseCase - Use case for updating an order (concurrent-safe)
 * Uses optimistic locking with retry mechanism for concurrent updates
 */
@Service
public class UpdateOrderUseCase {
    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;

    public UpdateOrderUseCase(OrderRepository orderRepository, EventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @Retryable(
            retryFor = {OptimisticLockException.class, org.springframework.dao.TransientDataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2, maxDelay = 2000)
    )
    public Order execute(OrderId orderId, OrderUpdateRequest request) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order id cannot be null");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (!order.canBeModified()) {
            throw new IllegalStateException("Order cannot be modified in current state: " + order.getStatus());
        }

        Order updatedOrder = orderRepository.save(order);

        updatedOrder.getDomainEvents().forEach(event -> {
            if (event instanceof com.informacolombia.prueba.domain.events.DomainEvent domainEvent) {
                eventPublisher.publish(domainEvent);
            }
        });
        updatedOrder.clearDomainEvents();

        return updatedOrder;
    }

    public record OrderUpdateRequest() {}
}
