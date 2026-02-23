package com.informacolombia.prueba.infrastructure.persistence;

import com.informacolombia.prueba.domain.entities.User;
import com.informacolombia.prueba.domain.repositories.UserRepository;
import com.informacolombia.prueba.domain.valueobjects.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JpaUserRepository - JPA implementation of UserRepository
 */
@Repository
public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepository {

    @Override
    @Query("SELECT u FROM User u WHERE u.userId.value = :userId")
    Optional<User> findById(@Param("userId") UserId userId);

    @Override
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    @Override
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Override
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username")
    boolean existsByUsername(@Param("username") String username);

    @Override
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);
}
