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
class CancelOrderUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EventPublisher eventPublisher;

    private CancelOrderUseCase cancelOrderUseCase;

    @BeforeEach
    void setUp() {
        cancelOrderUseCase = new CancelOrderUseCase(orderRepository, eventPublisher);
    }

    @Test
    void shouldCancelOrderSuccessfully() {
        OrderId orderId = OrderId.generate();
        UserId userId = UserId.generate();
        Order order = createPendingOrder(orderId, userId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = cancelOrderUseCase.execute(orderId);

        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
        verify(eventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    void shouldThrowExceptionWhenOrderIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            cancelOrderUseCase.execute(null);
        });

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        OrderId orderId = OrderId.generate();

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            cancelOrderUseCase.execute(orderId);
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
