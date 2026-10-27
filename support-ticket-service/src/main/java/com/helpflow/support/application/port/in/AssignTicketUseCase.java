package com.helpflow.support.application.port.in;

import com.helpflow.support.domain.model.Ticket;

public interface AssignTicketUseCase {
    Ticket assign(Long ticketId);
}
