package com.helpflow.support.domain.exception;

public class TicketNotFoundException extends DomainException {
    public TicketNotFoundException(Long id) {
        super("Ticket not found: " + id);
    }
}
