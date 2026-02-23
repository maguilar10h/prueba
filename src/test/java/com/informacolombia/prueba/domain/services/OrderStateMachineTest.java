package com.informacolombia.prueba.domain.services;

import com.informacolombia.prueba.domain.valueobjects.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OrderStateMachineTest {

    @Test
    void shouldAllowValidTransitions() {
        assertTrue(OrderStateMachine.isValidTransition(OrderStatus.PENDING, OrderStatus.CONFIRMED));
        assertTrue(OrderStateMachine.isValidTransition(OrderStatus.PENDING, OrderStatus.CANCELLED));
        assertTrue(OrderStateMachine.isValidTransition(OrderStatus.CONFIRMED, OrderStatus.PROCESSING));
        assertTrue(OrderStateMachine.isValidTransition(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        assertTrue(OrderStateMachine.isValidTransition(OrderStatus.CONFIRMED, OrderStatus.FAILED));
        assertTrue(OrderStateMachine.isValidTransition(OrderStatus.PROCESSING, OrderStatus.SHIPPED));
        assertTrue(OrderStateMachine.isValidTransition(OrderStatus.PROCESSING, OrderStatus.FAILED));
        assertTrue(OrderStateMachine.isValidTransition(OrderStatus.PROCESSING, OrderStatus.CANCELLED));
        assertTrue(OrderStateMachine.isValidTransition(OrderStatus.SHIPPED, OrderStatus.DELIVERED));
        assertTrue(OrderStateMachine.isValidTransition(OrderStatus.SHIPPED, OrderStatus.FAILED));
    }

    @Test
    void shouldNotAllowInvalidTransitions() {
        assertFalse(OrderStateMachine.isValidTransition(OrderStatus.PENDING, OrderStatus.PROCESSING));
        assertFalse(OrderStateMachine.isValidTransition(OrderStatus.PENDING, OrderStatus.SHIPPED));
        assertFalse(OrderStateMachine.isValidTransition(OrderStatus.CONFIRMED, OrderStatus.DELIVERED));
        assertFalse(OrderStateMachine.isValidTransition(OrderStatus.PROCESSING, OrderStatus.DELIVERED));
    }

    @Test
    void shouldNotAllowTransitionsFromFinalStates() {
        assertFalse(OrderStateMachine.isValidTransition(OrderStatus.DELIVERED, OrderStatus.PENDING));
        assertFalse(OrderStateMachine.isValidTransition(OrderStatus.DELIVERED, OrderStatus.CANCELLED));
        assertFalse(OrderStateMachine.isValidTransition(OrderStatus.CANCELLED, OrderStatus.PENDING));
        assertFalse(OrderStateMachine.isValidTransition(OrderStatus.FAILED, OrderStatus.PENDING));
    }

    @Test
    void shouldAllowSameStateTransition() {
        assertTrue(OrderStateMachine.isValidTransition(OrderStatus.PENDING, OrderStatus.PENDING));
        assertTrue(OrderStateMachine.isValidTransition(OrderStatus.CONFIRMED, OrderStatus.CONFIRMED));
    }

    @Test
    void shouldReturnFalseForNullStatuses() {
        assertFalse(OrderStateMachine.isValidTransition(null, OrderStatus.PENDING));
        assertFalse(OrderStateMachine.isValidTransition(OrderStatus.PENDING, null));
        assertFalse(OrderStateMachine.isValidTransition(null, null));
    }

    @Test
    void shouldIdentifyFinalStates() {
        assertTrue(OrderStateMachine.isFinalState(OrderStatus.DELIVERED));
        assertTrue(OrderStateMachine.isFinalState(OrderStatus.CANCELLED));
        assertTrue(OrderStateMachine.isFinalState(OrderStatus.FAILED));
    }

    @Test
    void shouldNotIdentifyNonFinalStatesAsFinal() {
        assertFalse(OrderStateMachine.isFinalState(OrderStatus.PENDING));
        assertFalse(OrderStateMachine.isFinalState(OrderStatus.CONFIRMED));
        assertFalse(OrderStateMachine.isFinalState(OrderStatus.PROCESSING));
        assertFalse(OrderStateMachine.isFinalState(OrderStatus.SHIPPED));
    }

    @Test
    void shouldReturnEmptySetForNullStatus() {
        Set<OrderStatus> nextStates = OrderStateMachine.getValidNextStates(null);
        assertTrue(nextStates.isEmpty());
    }

    @Test
    void shouldReturnEmptySetForFinalStates() {
        Set<OrderStatus> deliveredNext = OrderStateMachine.getValidNextStates(OrderStatus.DELIVERED);
        Set<OrderStatus> cancelledNext = OrderStateMachine.getValidNextStates(OrderStatus.CANCELLED);
        Set<OrderStatus> failedNext = OrderStateMachine.getValidNextStates(OrderStatus.FAILED);

        assertTrue(deliveredNext.isEmpty());
        assertTrue(cancelledNext.isEmpty());
        assertTrue(failedNext.isEmpty());
    }

    @Test
    void shouldReturnValidNextStates() {
        Set<OrderStatus> pendingNext = OrderStateMachine.getValidNextStates(OrderStatus.PENDING);
        assertEquals(2, pendingNext.size());
        assertTrue(pendingNext.contains(OrderStatus.CONFIRMED));
        assertTrue(pendingNext.contains(OrderStatus.CANCELLED));

        Set<OrderStatus> confirmedNext = OrderStateMachine.getValidNextStates(OrderStatus.CONFIRMED);
        assertEquals(3, confirmedNext.size());
        assertTrue(confirmedNext.contains(OrderStatus.PROCESSING));
        assertTrue(confirmedNext.contains(OrderStatus.CANCELLED));
        assertTrue(confirmedNext.contains(OrderStatus.FAILED));

        Set<OrderStatus> processingNext = OrderStateMachine.getValidNextStates(OrderStatus.PROCESSING);
        assertEquals(3, processingNext.size());
        assertTrue(processingNext.contains(OrderStatus.SHIPPED));
        assertTrue(processingNext.contains(OrderStatus.FAILED));
        assertTrue(processingNext.contains(OrderStatus.CANCELLED));

        Set<OrderStatus> shippedNext = OrderStateMachine.getValidNextStates(OrderStatus.SHIPPED);
        assertEquals(2, shippedNext.size());
        assertTrue(shippedNext.contains(OrderStatus.DELIVERED));
        assertTrue(shippedNext.contains(OrderStatus.FAILED));
    }
}
