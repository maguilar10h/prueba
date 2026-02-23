package com.informacolombia.prueba.application.ports.inbound;

import com.informacolombia.prueba.application.usecases.*;
import com.informacolombia.prueba.domain.entities.Order;
import com.informacolombia.prueba.domain.valueobjects.OrderId;
import com.informacolombia.prueba.domain.valueobjects.OrderStatus;
import com.informacolombia.prueba.domain.valueobjects.ProductId;
import com.informacolombia.prueba.domain.valueobjects.Quantity;
import com.informacolombia.prueba.domain.valueobjects.UserId;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * OrderInputPortImpl - Implementation of OrderInputPort
 */
@Service
public class OrderInputPortImpl implements OrderInputPort {
    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderUseCase updateOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;

    public OrderInputPortImpl(
            CreateOrderUseCase createOrderUseCase,
            GetOrderUseCase getOrderUseCase,
            UpdateOrderUseCase updateOrderUseCase,
            CancelOrderUseCase cancelOrderUseCase
    ) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.updateOrderUseCase = updateOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
    }

    @Override
    public Order createOrder(UserId userId, List<OrderItemRequest> items) {
        List<CreateOrderUseCase.OrderItemRequest> useCaseItems = items.stream()
                .map(item -> new CreateOrderUseCase.OrderItemRequest(
                        ProductId.of(item.productId()),
                        Quantity.of(item.quantity())
                ))
                .collect(Collectors.toList());
        return createOrderUseCase.execute(userId, useCaseItems);
    }

    @Override
    public Optional<Order> getOrder(OrderId orderId) {
        return getOrderUseCase.execute(orderId);
    }

    @Override
    public Order updateOrder(OrderId orderId, OrderUpdateRequest request) {
        return updateOrderUseCase.execute(orderId, new UpdateOrderUseCase.OrderUpdateRequest());
    }

    @Override
    public Order cancelOrder(OrderId orderId) {
        return cancelOrderUseCase.execute(orderId);
    }

    @Override
    public List<Order> searchOrders(OrderSearchRequest request) {
        // Simplified implementation - in production, use a proper search use case
        return List.of();
    }
}
