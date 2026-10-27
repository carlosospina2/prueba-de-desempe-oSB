package com.helpflow.support.application.service;

import com.helpflow.support.application.port.in.AssignTicketUseCase;
import com.helpflow.support.application.port.out.TechnicianDispatchPort;
import com.helpflow.support.application.port.out.TechnicianRepositoryPort;
import com.helpflow.support.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.support.application.port.out.TicketRepositoryPort;
import com.helpflow.support.domain.exception.BusinessRuleException;
import com.helpflow.support.domain.exception.TicketNotFoundException;
import com.helpflow.support.domain.model.Ticket;
import com.helpflow.support.domain.model.TicketHistory;
import com.helpflow.support.domain.model.enums.TicketStatus;
import com.helpflow.support.infrastructure.config.SlaConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignTicketService implements AssignTicketUseCase {

    private final TicketRepositoryPort ticketRepo;
    private final TechnicianRepositoryPort technicianRepo;
    private final TicketHistoryRepositoryPort historyRepo;
    private final TechnicianDispatchPort dispatchPort;
    private final SlaConfig slaConfig;

    public AssignTicketService(TicketRepositoryPort ticketRepo,
                               TechnicianRepositoryPort technicianRepo,
                               TicketHistoryRepositoryPort historyRepo,
                               TechnicianDispatchPort dispatchPort,
                               SlaConfig slaConfig) {
        this.ticketRepo = ticketRepo;
        this.technicianRepo = technicianRepo;
        this.historyRepo = historyRepo;
        this.dispatchPort = dispatchPort;
        this.slaConfig = slaConfig;
    }

    @Override
    @Transactional
    public Ticket assign(Long ticketId) {
        Ticket ticket = ticketRepo.findById(ticketId)
            .orElseThrow(() -> new TicketNotFoundException(ticketId));

        var technician = technicianRepo.findActiveBySpecialty(ticket.getCategory()).stream()
            .filter(t -> ticketRepo.countActiveByTechnicianId(t.getId()) < slaConfig.getMaxActiveTicketsPerTechnician())
            .findFirst()
            .orElseThrow(() -> new BusinessRuleException("No hay técnicos disponibles para esta categoría"));

        TicketStatus previous = ticket.getStatus();
        ticket.transitionTo(TicketStatus.ASIGNADO);
        ticket.setAssignedTechnicianId(technician.getId());
        Ticket saved = ticketRepo.save(ticket);
        historyRepo.save(new TicketHistory(ticketId, previous, TicketStatus.ASIGNADO,
            "Asignado manualmente a " + technician.getName(), "SYSTEM"));
        return saved;
    }
}
