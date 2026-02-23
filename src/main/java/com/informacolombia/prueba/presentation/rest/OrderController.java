package com.informacolombia.prueba.presentation.rest;

import com.informacolombia.prueba.application.ports.inbound.OrderInputPort;
import com.informacolombia.prueba.domain.entities.Order;
import com.informacolombia.prueba.domain.valueobjects.OrderId;
import com.informacolombia.prueba.presentation.rest.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OrderController - REST controller for order endpoints
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "API for managing orders")
@SecurityRequirement(name = "bearer-jwt")
public class OrderController {
    private final OrderInputPort orderInputPort;

    public OrderController(OrderInputPort orderInputPort) {
        this.orderInputPort = orderInputPort;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Create a new order", description = "Creates a new order with the provided items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<OrderResponse> createOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Order creation request", required = true)
            @RequestBody com.informacolombia.prueba.presentation.rest.dto.CreateOrderRequest request
    ) {
        // Convert DTO to use case request
        List<OrderInputPort.OrderItemRequest> items = request.items().stream()
                .map(item -> new OrderInputPort.OrderItemRequest(item.productId(), item.quantity()))
                .toList();

        // For now, using a placeholder userId - in production, extract from JWT
        var userId = com.informacolombia.prueba.domain.valueobjects.UserId.generate();
        Order order = orderInputPort.createOrder(userId, items);

        OrderResponse response = new OrderResponse(
                order.getOrderId().getValue(),
                order.getStatus().name(),
                order.getTotalAmount().getAmount().toString()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get order by ID", description = "Retrieves an order by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(description = "Order ID", required = true, example = "order-123")
            @PathVariable String id) {
        OrderId orderId = com.informacolombia.prueba.domain.valueobjects.OrderId.of(id);
        return orderInputPort.getOrder(orderId)
                .map(order -> {
                    OrderResponse response = new OrderResponse(
                            order.getOrderId().getValue(),
                            order.getStatus().name(),
                            order.getTotalAmount().getAmount().toString()
                    );
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an order", description = "Updates an existing order (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order updated successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<OrderResponse> updateOrder(
            @Parameter(description = "Order ID", required = true, example = "order-123")
            @PathVariable String id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Order update request", required = true)
            @RequestBody OrderInputPort.OrderUpdateRequest request
    ) {
        OrderId orderId = com.informacolombia.prueba.domain.valueobjects.OrderId.of(id);
        Order order = orderInputPort.updateOrder(orderId, request);
        OrderResponse response = new OrderResponse(
                order.getOrderId().getValue(),
                order.getStatus().name(),
                order.getTotalAmount().getAmount().toString()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel an order", description = "Cancels an existing order (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<Void> cancelOrder(
            @Parameter(description = "Order ID", required = true, example = "order-123")
            @PathVariable String id) {
        OrderId orderId = com.informacolombia.prueba.domain.valueobjects.OrderId.of(id);
        orderInputPort.cancelOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search orders", description = "Searches orders with optional filters (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<List<OrderResponse>> searchOrders(
            @Parameter(description = "Filter by user ID", example = "user-123")
            @RequestParam(required = false) String userId,
            @Parameter(description = "Filter by order status", example = "PENDING")
            @RequestParam(required = false) String status,
            @Parameter(description = "Filter by start date", example = "2024-01-01")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "Filter by end date", example = "2024-12-31")
            @RequestParam(required = false) String endDate
    ) {
        OrderInputPort.OrderSearchRequest request = new OrderInputPort.OrderSearchRequest(
                userId, status, startDate, endDate
        );
        List<Order> orders = orderInputPort.searchOrders(request);
        List<OrderResponse> result = orders.stream()
                .map(order -> new OrderResponse(
                        order.getOrderId().getValue(),
                        order.getStatus().name(),
                        order.getTotalAmount().getAmount().toString()
                ))
                .toList();
        return ResponseEntity.ok(result);
    }
}
