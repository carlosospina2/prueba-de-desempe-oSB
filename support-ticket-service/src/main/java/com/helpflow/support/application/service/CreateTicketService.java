package com.helpflow.support.application.service;

import com.helpflow.support.application.port.in.CreateTicketUseCase;
import com.helpflow.support.application.port.out.*;
import com.helpflow.support.domain.exception.BusinessRuleException;
import com.helpflow.support.domain.exception.UserNotFoundException;
import com.helpflow.support.domain.model.*;
import com.helpflow.support.domain.model.enums.*;
import com.helpflow.support.infrastructure.config.SlaConfig;
import io.micrometer.core.instrument.Counter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class CreateTicketService implements CreateTicketUseCase {

    private final TicketRepositoryPort ticketRepo;
    private final UserRepositoryPort userRepo;
    private final TechnicianRepositoryPort technicianRepo;
    private final TicketHistoryRepositoryPort historyRepo;
    private final TechnicianDispatchPort dispatchPort;
    private final SlaConfig slaConfig;
    private final Counter ticketsCreatedCounter;
    private final Counter dispatchCallsCounter;

    public CreateTicketService(TicketRepositoryPort ticketRepo,
                               UserRepositoryPort userRepo,
                               TechnicianRepositoryPort technicianRepo,
                               TicketHistoryRepositoryPort historyRepo,
                               TechnicianDispatchPort dispatchPort,
                               SlaConfig slaConfig,
                               Counter ticketsCreatedCounter,
                               Counter dispatchCallsCounter) {
        this.ticketRepo = ticketRepo;
        this.userRepo = userRepo;
        this.technicianRepo = technicianRepo;
        this.historyRepo = historyRepo;
        this.dispatchPort = dispatchPort;
        this.slaConfig = slaConfig;
        this.ticketsCreatedCounter = ticketsCreatedCounter;
        this.dispatchCallsCounter = dispatchCallsCounter;
    }

    @Override
    @Transactional
    public Ticket create(Long userId, String title, String description,
                         TicketCategory category, TicketPriority priority,
                         String sede, String affectedAsset) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(String.valueOf(userId)));
        if (!user.isActive()) {
            throw new BusinessRuleException("Solo usuarios activos pueden registrar tickets");
        }

        Ticket ticket = new Ticket();
        ticket.setUserId(userId);
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setCategory(category);
        ticket.setPriority(priority);
        ticket.setSede(sede);
        ticket.setAffectedAsset(affectedAsset);
        ticket.setCreatedAt(Instant.now());
        ticket.setStatus(TicketStatus.ABIERTO);

        int slaHours = slaConfig.getHoursPerPriority().getOrDefault(priority.name(), 24);
        ticket.setSlaDeadline(Instant.now().plus(slaHours, ChronoUnit.HOURS));

        Ticket saved = ticketRepo.save(ticket);
        ticketsCreatedCounter.increment();

        historyRepo.save(new TicketHistory(saved.getId(), null, TicketStatus.ABIERTO,
            "Ticket creado", user.getEmail()));

        // Attempt assignment via dispatch service
        try {
            dispatchCallsCounter.increment();
            TechnicianDispatchPort.DispatchResult dispatch = dispatchPort.dispatch(
                saved.getId(), category.name(), priority.name());

            // Find internal technician matching dispatch suggestion
            technicianRepo.findActiveBySpecialty(category).stream()
                .filter(t -> t.isActive())
                .filter(t -> ticketRepo.countActiveByTechnicianId(t.getId()) < slaConfig.getMaxActiveTicketsPerTechnician())
                .findFirst()
                .ifPresent(technician -> {
                    TicketStatus prev = saved.getStatus();
                    saved.transitionTo(TicketStatus.ASIGNADO);
                    saved.setAssignedTechnicianId(technician.getId());
                    ticketRepo.save(saved);
                    historyRepo.save(new TicketHistory(saved.getId(), prev, TicketStatus.ASIGNADO,
                        "Asignado a " + technician.getName() + " via dispatch", user.getEmail()));
                });
        } catch (Exception e) {
            // Dispatch failure does not block ticket creation
        }

        return saved;
    }
}
