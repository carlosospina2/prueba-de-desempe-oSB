package com.helpflow.support.application.port.out;

import com.helpflow.support.domain.model.Technician;
import com.helpflow.support.domain.model.enums.TicketCategory;
import java.util.List;
import java.util.Optional;

public interface TechnicianRepositoryPort {
    Technician save(Technician technician);
    Optional<Technician> findById(Long id);
    Optional<Technician> findByDocument(String document);
    List<Technician> findActiveBySpecialty(TicketCategory category);
    List<Technician> findAll();
    boolean existsByDocument(String document);
}
