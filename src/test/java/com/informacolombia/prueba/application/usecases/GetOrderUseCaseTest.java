package com.informacolombia.prueba.application.usecases;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetOrderUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    private GetOrderUseCase getOrderUseCase;

    @BeforeEach
    void setUp() {
        getOrderUseCase = new GetOrderUseCase(orderRepository);
    }

    @Test
    void shouldGetOrderSuccessfully() {
        OrderId orderId = OrderId.generate();
        UserId userId = UserId.generate();
        Order order = createPendingOrder(orderId, userId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Optional<Order> result = getOrderUseCase.execute(orderId);

        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().getOrderId());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void shouldReturnEmptyWhenOrderNotFound() {
        OrderId orderId = OrderId.generate();

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        Optional<Order> result = getOrderUseCase.execute(orderId);

        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void shouldThrowExceptionWhenOrderIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            getOrderUseCase.execute(null);
        });

        verify(orderRepository, never()).findById(any());
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
