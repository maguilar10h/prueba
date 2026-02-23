package com.informacolombia.prueba.domain.services;

import com.informacolombia.prueba.domain.valueobjects.OrderStatus;

import java.util.EnumSet;
import java.util.Set;

/**
 * OrderStateMachine - Domain service that manages order state transitions
 */
public class OrderStateMachine {

    private static final Set<OrderStatus> FINAL_STATES = EnumSet.of(
            OrderStatus.DELIVERED,
            OrderStatus.CANCELLED,
            OrderStatus.FAILED
    );

    /**
     * Validates if a transition from oldStatus to newStatus is allowed
     */
    public static boolean isValidTransition(OrderStatus oldStatus, OrderStatus newStatus) {
        if (oldStatus == null || newStatus == null) {
            return false;
        }

        // Cannot transition from a final state
        if (FINAL_STATES.contains(oldStatus)) {
            return false;
        }

        // Same state is always valid (no-op)
        if (oldStatus == newStatus) {
            return true;
        }

        return switch (oldStatus) {
            case PENDING -> newStatus == OrderStatus.CONFIRMED || 
                           newStatus == OrderStatus.CANCELLED;
            case CONFIRMED -> newStatus == OrderStatus.PROCESSING || 
                             newStatus == OrderStatus.CANCELLED ||
                             newStatus == OrderStatus.FAILED;
            case PROCESSING -> newStatus == OrderStatus.SHIPPED || 
                              newStatus == OrderStatus.FAILED ||
                              newStatus == OrderStatus.CANCELLED;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED || 
                           newStatus == OrderStatus.FAILED;
            default -> false;
        };
    }

    /**
     * Checks if a status is a final state
     */
    public static boolean isFinalState(OrderStatus status) {
        return FINAL_STATES.contains(status);
    }

    /**
     * Gets all valid next states from a given status
     */
    public static Set<OrderStatus> getValidNextStates(OrderStatus currentStatus) {
        if (currentStatus == null) {
            return Set.of();
        }

        if (FINAL_STATES.contains(currentStatus)) {
            return Set.of();
        }

        return switch (currentStatus) {
            case PENDING -> EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED);
            case CONFIRMED -> EnumSet.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED, OrderStatus.FAILED);
            case PROCESSING -> EnumSet.of(OrderStatus.SHIPPED, OrderStatus.FAILED, OrderStatus.CANCELLED);
            case SHIPPED -> EnumSet.of(OrderStatus.DELIVERED, OrderStatus.FAILED);
            default -> Set.of();
        };
    }
}
