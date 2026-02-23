package com.informacolombia.prueba.domain.repositories;

import com.informacolombia.prueba.domain.entities.Inventory;
import com.informacolombia.prueba.domain.valueobjects.OrderId;
import com.informacolombia.prueba.domain.valueobjects.ProductId;

import java.util.List;
import java.util.Optional;

/**
 * InventoryRepository - Output port for inventory persistence
 */
public interface InventoryRepository {
    /**
     * Saves an inventory reservation
     */
    Inventory save(Inventory inventory);

    /**
     * Finds inventory by ID
     */
    Optional<Inventory> findById(Long id);

    /**
     * Finds all inventory reservations for an order
     */
    List<Inventory> findByOrderId(OrderId orderId);

    /**
     * Finds all inventory reservations for a product
     */
    List<Inventory> findByProductId(ProductId productId);

    /**
     * Finds active (not released) inventory reservations for an order
     */
    List<Inventory> findActiveByOrderId(OrderId orderId);

    /**
     * Deletes inventory reservation
     */
    void delete(Inventory inventory);
}
