package com.informacolombia.prueba.infrastructure.persistence;

import com.informacolombia.prueba.domain.entities.Order;
import com.informacolombia.prueba.domain.repositories.OrderRepository;
import com.informacolombia.prueba.domain.valueobjects.OrderId;
import com.informacolombia.prueba.domain.valueobjects.OrderStatus;
import com.informacolombia.prueba.domain.valueobjects.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * JpaOrderRepository - JPA implementation of OrderRepository
 */
@Repository
public interface JpaOrderRepository extends JpaRepository<Order, Long>, OrderRepository {

    @Override
    @Query("SELECT o FROM Order o WHERE o.orderId.value = :orderId")
    Optional<Order> findById(@Param("orderId") OrderId orderId);

    @Override
    @Query("SELECT o FROM Order o WHERE o.userId.value = :userId")
    List<Order> findByUserId(@Param("userId") UserId userId);

    @Override
    @Query("SELECT o FROM Order o WHERE o.status = :status")
    List<Order> findByStatus(@Param("status") OrderStatus status);

    @Override
    @Query("SELECT o FROM Order o WHERE o.userId.value = :userId AND o.status = :status")
    List<Order> findByUserIdAndStatus(@Param("userId") UserId userId, @Param("status") OrderStatus status);

    @Override
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByCreatedAtBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
}
