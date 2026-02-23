package com.informacolombia.prueba.application.usecases;

import com.informacolombia.prueba.domain.entities.Order;
import com.informacolombia.prueba.domain.repositories.OrderRepository;
import com.informacolombia.prueba.domain.valueobjects.OrderId;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * GetOrderUseCase - Use case for retrieving an order
 */
@Service
public class GetOrderUseCase {
    private final OrderRepository orderRepository;

    public GetOrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Optional<Order> execute(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order id cannot be null");
        }
        return orderRepository.findById(orderId);
    }
}
