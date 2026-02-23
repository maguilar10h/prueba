package com.informacolombia.prueba.application.usecases;

import com.informacolombia.prueba.application.ports.outbound.EventPublisher;
import com.informacolombia.prueba.domain.entities.Order;
import com.informacolombia.prueba.domain.entities.OrderItem;
import com.informacolombia.prueba.domain.repositories.OrderRepository;
import com.informacolombia.prueba.domain.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateOrderUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EventPublisher eventPublisher;

    private UpdateOrderUseCase updateOrderUseCase;

    @BeforeEach
    void setUp() {
        updateOrderUseCase = new UpdateOrderUseCase(orderRepository, eventPublisher);
    }

    @Test
    void shouldUpdateOrderSuccessfully() {
        OrderId orderId = OrderId.generate();
        UserId userId = UserId.generate();
        Order order = createPendingOrder(orderId, userId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateOrderUseCase.OrderUpdateRequest request = new UpdateOrderUseCase.OrderUpdateRequest();
        Order result = updateOrderUseCase.execute(orderId, request);

        assertNotNull(result);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
        verify(eventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    void shouldThrowExceptionWhenOrderIdIsNull() {
        UpdateOrderUseCase.OrderUpdateRequest request = new UpdateOrderUseCase.OrderUpdateRequest();

        assertThrows(IllegalArgumentException.class, () -> {
            updateOrderUseCase.execute(null, request);
        });

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        OrderId orderId = OrderId.generate();
        UpdateOrderUseCase.OrderUpdateRequest request = new UpdateOrderUseCase.OrderUpdateRequest();

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            updateOrderUseCase.execute(orderId, request);
        });

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenOrderCannotBeModified() {
        OrderId orderId = OrderId.generate();
        UserId userId = UserId.generate();
        Order order = createPendingOrder(orderId, userId);
        order.confirm();
        order.startProcessing();
        order.ship();
        order.deliver();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        UpdateOrderUseCase.OrderUpdateRequest request = new UpdateOrderUseCase.OrderUpdateRequest();

        assertThrows(IllegalStateException.class, () -> {
            updateOrderUseCase.execute(orderId, request);
        });

        verify(orderRepository, never()).save(any());
    }

    private Order createPendingOrder(OrderId orderId, UserId userId) {
        ProductId productId = ProductId.generate();
        OrderItem item = new OrderItem(
                productId,
                Quantity.of(1),
                Money.of(10.0, "USD")
        );
        return new Order(orderId, userId, List.of(item));
    }
}
