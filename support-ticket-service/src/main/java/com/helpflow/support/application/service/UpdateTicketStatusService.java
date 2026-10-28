package com.helpflow.support.application.service;

import com.helpflow.support.application.port.in.UpdateTicketStatusUseCase;
import com.helpflow.support.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.support.application.port.out.TicketRepositoryPort;
import com.helpflow.support.domain.exception.TicketNotFoundException;
import com.helpflow.support.domain.model.Ticket;
import com.helpflow.support.domain.model.TicketHistory;
import com.helpflow.support.domain.model.enums.TicketStatus;
import io.micrometer.core.instrument.Counter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateTicketStatusService implements UpdateTicketStatusUseCase {

    private final TicketRepositoryPort ticketRepo;
    private final TicketHistoryRepositoryPort historyRepo;
    private final Counter ticketsResolvedCounter;

    public UpdateTicketStatusService(TicketRepositoryPort ticketRepo,
                                     TicketHistoryRepositoryPort historyRepo,
                                     Counter ticketsResolvedCounter) {
        this.ticketRepo = ticketRepo;
        this.historyRepo = historyRepo;
        this.ticketsResolvedCounter = ticketsResolvedCounter;
    }

    @Override
    @Transactional
    public Ticket updateStatus(Long ticketId, TicketStatus newStatus,
                               String observation, String changedBy) {
        Ticket ticket = ticketRepo.findById(ticketId)
            .orElseThrow(() -> new TicketNotFoundException(ticketId));
        TicketStatus previous = ticket.getStatus();
        ticket.transitionTo(newStatus);
        Ticket saved = ticketRepo.save(ticket);
        historyRepo.save(new TicketHistory(ticketId, previous, newStatus, observation, changedBy));

        // Increment the resolved counter only when the ticket reaches RESUELTO
        if (newStatus == TicketStatus.RESUELTO) {
            ticketsResolvedCounter.increment();
        }

        return saved;
    }
}
