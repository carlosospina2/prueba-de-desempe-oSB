package com.helpflow.support.infrastructure.adapter.out.persistence;

import com.helpflow.support.application.port.out.TicketRepositoryPort;
import com.helpflow.support.domain.model.Ticket;
import com.helpflow.support.infrastructure.adapter.out.persistence.entity.TicketEntity;
import com.helpflow.support.infrastructure.adapter.out.persistence.mapper.TicketMapper;
import com.helpflow.support.infrastructure.adapter.out.persistence.repository.JpaTicketRepository;
import com.helpflow.support.infrastructure.adapter.out.persistence.repository.JpaUserRepository;
import com.helpflow.support.infrastructure.adapter.out.persistence.repository.JpaTechnicianRepository;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TicketPersistenceAdapter implements TicketRepositoryPort {
    private final JpaTicketRepository repo;
    private final JpaUserRepository userRepo;
    private final JpaTechnicianRepository techRepo;
    private final TicketMapper mapper;

    public TicketPersistenceAdapter(JpaTicketRepository repo, JpaUserRepository userRepo,
                                    JpaTechnicianRepository techRepo, TicketMapper mapper) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.techRepo = techRepo;
        this.mapper = mapper;
    }

    @Override
    public Ticket save(Ticket ticket) {
        TicketEntity entity = mapper.toEntity(ticket);
        if (ticket.getUserId() != null) {
            userRepo.findById(ticket.getUserId()).ifPresent(entity::setUser);
        }
        if (ticket.getAssignedTechnicianId() != null) {
            techRepo.findById(ticket.getAssignedTechnicianId()).ifPresent(entity::setTechnician);
        }
        return mapper.toDomain(repo.save(entity));
    }

    @Override public Optional<Ticket> findById(Long id) { return repo.findById(id).map(mapper::toDomain); }
    @Override public List<Ticket> findByUserId(Long userId) { return repo.findByUserId(userId).stream().map(mapper::toDomain).collect(Collectors.toList()); }
    @Override public List<Ticket> findByAssignedTechnicianId(Long techId) { return repo.findByTechnicianId(techId).stream().map(mapper::toDomain).collect(Collectors.toList()); }
    @Override public List<Ticket> findActiveByTechnicianId(Long techId) { return repo.findActiveByTechnicianId(techId).stream().map(mapper::toDomain).collect(Collectors.toList()); }
    @Override public List<Ticket> findAll() { return repo.findAll().stream().map(mapper::toDomain).collect(Collectors.toList()); }
    @Override public int countActiveByTechnicianId(Long techId) { return repo.countActiveByTechnicianId(techId); }
    @Override public List<Ticket> findNearSlaDeadline(int thresholdMinutes) {
        return repo.findNearSlaDeadline(Instant.now().plusSeconds(thresholdMinutes * 60L))
            .stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
