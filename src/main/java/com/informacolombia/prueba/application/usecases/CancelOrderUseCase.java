package com.informacolombia.prueba.application.usecases;

import com.informacolombia.prueba.application.ports.outbound.EventPublisher;
import com.informacolombia.prueba.domain.entities.Order;
import com.informacolombia.prueba.domain.repositories.OrderRepository;
import com.informacolombia.prueba.domain.valueobjects.OrderId;
import org.springframework.stereotype.Service;

/**
 * CancelOrderUseCase - Use case for canceling an order
 */
@Service
public class CancelOrderUseCase {
    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;

    public CancelOrderUseCase(OrderRepository orderRepository, EventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    public Order execute(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order id cannot be null");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        order.cancel();
        Order savedOrder = orderRepository.save(order);

        savedOrder.getDomainEvents().forEach(event -> {
            if (event instanceof com.informacolombia.prueba.domain.events.DomainEvent domainEvent) {
                eventPublisher.publish(domainEvent);
            }
        });
        savedOrder.clearDomainEvents();

        return savedOrder;
    }
}
