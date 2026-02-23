package com.informacolombia.prueba.infrastructure.persistence;

import com.informacolombia.prueba.domain.entities.Product;
import com.informacolombia.prueba.domain.repositories.ProductRepository;
import com.informacolombia.prueba.domain.valueobjects.ProductId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JpaProductRepository - JPA implementation of ProductRepository
 */
@Repository
public interface JpaProductRepository extends JpaRepository<Product, Long>, ProductRepository {

    @Override
    @Query("SELECT p FROM Product p WHERE p.productId.value = :productId")
    Optional<Product> findById(@Param("productId") ProductId productId);
}
