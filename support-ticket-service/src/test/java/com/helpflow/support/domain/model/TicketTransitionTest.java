package com.helpflow.support.domain.model;

import com.helpflow.support.domain.exception.InvalidTicketTransitionException;
import com.helpflow.support.domain.model.enums.TicketStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TicketTransitionTest {

    @Test
    void shouldAllowValidTransition() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.ABIERTO);
        ticket.transitionTo(TicketStatus.ASIGNADO);
        assertEquals(TicketStatus.ASIGNADO, ticket.getStatus());
    }

    @Test
    void shouldRejectInvalidTransition() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.CERRADO);
        assertThrows(InvalidTicketTransitionException.class, () ->
            ticket.transitionTo(TicketStatus.ABIERTO));
    }

    @Test
    void shouldAllowCancelFromOpen() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.ABIERTO);
        ticket.transitionTo(TicketStatus.CANCELADO);
        assertEquals(TicketStatus.CANCELADO, ticket.getStatus());
    }

    @Test
    void shouldNotAllowTransitionFromCancelled() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.CANCELADO);
        assertThrows(InvalidTicketTransitionException.class, () ->
            ticket.transitionTo(TicketStatus.ABIERTO));
    }
}
