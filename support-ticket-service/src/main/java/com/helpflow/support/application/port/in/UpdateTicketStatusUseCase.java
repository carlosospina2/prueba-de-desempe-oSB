package com.helpflow.support.application.port.in;

import com.helpflow.support.domain.model.Ticket;
import com.helpflow.support.domain.model.enums.TicketStatus;

public interface UpdateTicketStatusUseCase {
    Ticket updateStatus(Long ticketId, TicketStatus newStatus, String observation, String changedBy);
}
