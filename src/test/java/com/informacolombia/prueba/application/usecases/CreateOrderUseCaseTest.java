package com.informacolombia.prueba.application.usecases;

import com.informacolombia.prueba.application.ports.outbound.EventPublisher;
import com.informacolombia.prueba.application.saga.OrderSaga;
import com.informacolombia.prueba.domain.entities.Inventory;
import com.informacolombia.prueba.domain.entities.Order;
import com.informacolombia.prueba.domain.entities.Product;
import com.informacolombia.prueba.domain.repositories.OrderRepository;
import com.informacolombia.prueba.domain.repositories.ProductRepository;
import com.informacolombia.prueba.domain.valueobjects.Money;
import com.informacolombia.prueba.domain.valueobjects.OrderId;
import com.informacolombia.prueba.domain.valueobjects.ProductId;
import com.informacolombia.prueba.domain.valueobjects.Quantity;
import com.informacolombia.prueba.domain.valueobjects.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ReserveInventoryUseCase reserveInventoryUseCase;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private OrderSaga orderSaga;

    private CreateOrderUseCase createOrderUseCase;

    @BeforeEach
    void setUp() {
        createOrderUseCase = new CreateOrderUseCase(
                orderRepository,
                productRepository,
                reserveInventoryUseCase,
                eventPublisher,
                orderSaga
        );
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        UserId userId = UserId.generate();
        ProductId productId = ProductId.generate();
        Product product = new Product(
                productId,
                "Test Product",
                "Description",
                Money.of(10.0, "USD"),
                Quantity.of(100)
        );

        CreateOrderUseCase.OrderItemRequest itemRequest = new CreateOrderUseCase.OrderItemRequest(
                productId,
                Quantity.of(2)
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reserveInventoryUseCase.execute(any(OrderId.class), any(ProductId.class), any(Quantity.class)))
                .thenReturn(new Inventory(OrderId.generate(), productId, Quantity.of(2)));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = createOrderUseCase.execute(userId, List.of(itemRequest));

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertFalse(result.getItems().isEmpty());
        assertEquals(1, result.getItems().size());
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(reserveInventoryUseCase, times(1)).execute(any(OrderId.class), eq(productId), eq(Quantity.of(2)));
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(eventPublisher, atLeastOnce()).publish(any());
        verify(orderSaga, never()).compensateOrderCreation(any());
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        ProductId productId = ProductId.generate();
        CreateOrderUseCase.OrderItemRequest itemRequest = new CreateOrderUseCase.OrderItemRequest(
                productId,
                Quantity.of(2)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            createOrderUseCase.execute(null, List.of(itemRequest));
        });

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenItemsIsNull() {
        UserId userId = UserId.generate();

        assertThrows(IllegalArgumentException.class, () -> {
            createOrderUseCase.execute(userId, null);
        });

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenItemsIsEmpty() {
        UserId userId = UserId.generate();

        assertThrows(IllegalArgumentException.class, () -> {
            createOrderUseCase.execute(userId, List.of());
        });

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        UserId userId = UserId.generate();
        ProductId productId = ProductId.generate();
        CreateOrderUseCase.OrderItemRequest itemRequest = new CreateOrderUseCase.OrderItemRequest(
                productId,
                Quantity.of(2)
        );

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            createOrderUseCase.execute(userId, List.of(itemRequest));
        });

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenInsufficientQuantity() {
        UserId userId = UserId.generate();
        ProductId productId = ProductId.generate();
        Product product = new Product(
                productId,
                "Test Product",
                "Description",
                Money.of(10.0, "USD"),
                Quantity.of(5)
        );

        CreateOrderUseCase.OrderItemRequest itemRequest = new CreateOrderUseCase.OrderItemRequest(
                productId,
                Quantity.of(10)
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(IllegalStateException.class, () -> {
            createOrderUseCase.execute(userId, List.of(itemRequest));
        });

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldPublishDomainEvents() {
        UserId userId = UserId.generate();
        ProductId productId = ProductId.generate();
        Product product = new Product(
                productId,
                "Test Product",
                "Description",
                Money.of(10.0, "USD"),
                Quantity.of(100)
        );

        CreateOrderUseCase.OrderItemRequest itemRequest = new CreateOrderUseCase.OrderItemRequest(
                productId,
                Quantity.of(2)
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reserveInventoryUseCase.execute(any(OrderId.class), any(ProductId.class), any(Quantity.class)))
                .thenReturn(new Inventory(OrderId.generate(), productId, Quantity.of(2)));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        createOrderUseCase.execute(userId, List.of(itemRequest));

        ArgumentCaptor<com.informacolombia.prueba.domain.events.DomainEvent> eventCaptor =
                ArgumentCaptor.forClass(com.informacolombia.prueba.domain.events.DomainEvent.class);
        verify(eventPublisher, atLeastOnce()).publish(eventCaptor.capture());
    }

    @Test
    void shouldRollbackWhenOrderSaveFails() {
        UserId userId = UserId.generate();
        ProductId productId = ProductId.generate();
        Product product = new Product(
                productId,
                "Test Product",
                "Description",
                Money.of(10.0, "USD"),
                Quantity.of(100)
        );

        CreateOrderUseCase.OrderItemRequest itemRequest = new CreateOrderUseCase.OrderItemRequest(
                productId,
                Quantity.of(2)
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            return p;
        });
        when(reserveInventoryUseCase.execute(any(OrderId.class), any(ProductId.class), any(Quantity.class)))
                .thenReturn(new Inventory(OrderId.generate(), productId, Quantity.of(2)));
        when(orderRepository.save(any(Order.class))).thenThrow(new RuntimeException("Database error"));
        when(orderRepository.findById(any(OrderId.class))).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            createOrderUseCase.execute(userId, List.of(itemRequest));
        });

        verify(productRepository, times(2)).save(any(Product.class));
        verify(orderSaga, times(1)).compensateOrderCreation(any());
    }

    @Test
    void shouldRollbackWhenProductSaveFails() {
        UserId userId = UserId.generate();
        ProductId productId = ProductId.generate();
        Product product = new Product(
                productId,
                "Test Product",
                "Description",
                Money.of(10.0, "USD"),
                Quantity.of(100)
        );

        CreateOrderUseCase.OrderItemRequest itemRequest = new CreateOrderUseCase.OrderItemRequest(
                productId,
                Quantity.of(2)
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            createOrderUseCase.execute(userId, List.of(itemRequest));
        });

        verify(productRepository, times(1)).save(any(Product.class));
        verify(reserveInventoryUseCase, never()).execute(any(), any(), any());
        verify(orderRepository, never()).save(any());
        verify(orderSaga, times(1)).compensateOrderCreation(any());
    }

    @Test
    void shouldRollbackWhenInventoryReservationFails() {
        UserId userId = UserId.generate();
        ProductId productId = ProductId.generate();
        Product product = new Product(
                productId,
                "Test Product",
                "Description",
                Money.of(10.0, "USD"),
                Quantity.of(100)
        );

        CreateOrderUseCase.OrderItemRequest itemRequest = new CreateOrderUseCase.OrderItemRequest(
                productId,
                Quantity.of(2)
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reserveInventoryUseCase.execute(any(OrderId.class), any(ProductId.class), any(Quantity.class)))
                .thenThrow(new RuntimeException("Inventory reservation failed"));

        assertThrows(RuntimeException.class, () -> {
            createOrderUseCase.execute(userId, List.of(itemRequest));
        });

        verify(productRepository, times(2)).save(any(Product.class));
        verify(orderRepository, never()).save(any());
        verify(orderSaga, times(1)).compensateOrderCreation(any());
    }

    @Test
    void shouldReleaseProductQuantityOnRollback() {
        UserId userId = UserId.generate();
        ProductId productId = ProductId.generate();
        Product product = new Product(
                productId,
                "Test Product",
                "Description",
                Money.of(10.0, "USD"),
                Quantity.of(100)
        );

        CreateOrderUseCase.OrderItemRequest itemRequest = new CreateOrderUseCase.OrderItemRequest(
                productId,
                Quantity.of(20)
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reserveInventoryUseCase.execute(any(OrderId.class), any(ProductId.class), any(Quantity.class)))
                .thenReturn(new Inventory(OrderId.generate(), productId, Quantity.of(20)));
        when(orderRepository.save(any(Order.class))).thenThrow(new RuntimeException("Database error"));
        when(orderRepository.findById(any(OrderId.class))).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            createOrderUseCase.execute(userId, List.of(itemRequest));
        });

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(2)).save(productCaptor.capture());
        
        List<Product> savedProducts = productCaptor.getAllValues();
        Product firstSave = savedProducts.get(0);
        Product rollbackSave = savedProducts.get(1);
        
        assertEquals(Quantity.of(80), firstSave.getAvailableQuantity());
        assertEquals(Quantity.of(100), rollbackSave.getAvailableQuantity());
    }

    @Test
    void shouldReserveInventoryForEachItem() {
        UserId userId = UserId.generate();
        ProductId productId1 = ProductId.generate();
        ProductId productId2 = ProductId.generate();
        
        Product product1 = new Product(productId1, "Product 1", "Desc", Money.of(10.0, "USD"), Quantity.of(100));
        Product product2 = new Product(productId2, "Product 2", "Desc", Money.of(20.0, "USD"), Quantity.of(50));

        CreateOrderUseCase.OrderItemRequest item1 = new CreateOrderUseCase.OrderItemRequest(productId1, Quantity.of(2));
        CreateOrderUseCase.OrderItemRequest item2 = new CreateOrderUseCase.OrderItemRequest(productId2, Quantity.of(3));

        when(productRepository.findById(productId1)).thenReturn(Optional.of(product1));
        when(productRepository.findById(productId2)).thenReturn(Optional.of(product2));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reserveInventoryUseCase.execute(any(OrderId.class), any(ProductId.class), any(Quantity.class)))
                .thenReturn(new Inventory(OrderId.generate(), ProductId.generate(), Quantity.of(1)));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = createOrderUseCase.execute(userId, List.of(item1, item2));

        assertNotNull(result);
        assertEquals(2, result.getItems().size());
        verify(reserveInventoryUseCase, times(2)).execute(any(OrderId.class), any(ProductId.class), any(Quantity.class));
        verify(productRepository, times(2)).save(any(Product.class));
    }
}
