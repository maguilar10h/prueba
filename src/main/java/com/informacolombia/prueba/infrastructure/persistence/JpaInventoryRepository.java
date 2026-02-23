package com.informacolombia.prueba.infrastructure.persistence;

import com.informacolombia.prueba.domain.entities.Inventory;
import com.informacolombia.prueba.domain.repositories.InventoryRepository;
import com.informacolombia.prueba.domain.valueobjects.OrderId;
import com.informacolombia.prueba.domain.valueobjects.ProductId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JpaInventoryRepository - JPA implementation of InventoryRepository
 */
@Repository
public interface JpaInventoryRepository extends JpaRepository<Inventory, Long>, InventoryRepository {

    @Override
    @Query("SELECT i FROM Inventory i WHERE i.orderId = :orderId")
    List<Inventory> findByOrderId(@Param("orderId") OrderId orderId);

    @Override
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId")
    List<Inventory> findByProductId(@Param("productId") ProductId productId);

    @Override
    @Query("SELECT i FROM Inventory i WHERE i.orderId = :orderId AND i.released = false")
    List<Inventory> findActiveByOrderId(@Param("orderId") OrderId orderId);
}
