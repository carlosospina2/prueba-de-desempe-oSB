package com.helpflow.support.application.port.out;

import com.helpflow.support.domain.model.Ticket;
import com.helpflow.support.domain.model.enums.TicketStatus;
import java.util.List;
import java.util.Optional;

public interface TicketRepositoryPort {
    Ticket save(Ticket ticket);
    Optional<Ticket> findById(Long id);
    List<Ticket> findByUserId(Long userId);
    List<Ticket> findByAssignedTechnicianId(Long technicianId);
    List<Ticket> findActiveByTechnicianId(Long technicianId);
    List<Ticket> findAll();
    int countActiveByTechnicianId(Long technicianId);
    List<Ticket> findNearSlaDeadline(int thresholdMinutes);
}
