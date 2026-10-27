package com.helpflow.support.infrastructure.adapter.out.persistence;

import com.helpflow.support.application.port.out.TechnicianRepositoryPort;
import com.helpflow.support.domain.model.Technician;
import com.helpflow.support.domain.model.enums.TicketCategory;
import com.helpflow.support.domain.model.enums.UserStatus;
import com.helpflow.support.infrastructure.adapter.out.persistence.mapper.TechnicianMapper;
import com.helpflow.support.infrastructure.adapter.out.persistence.repository.JpaTechnicianRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TechnicianPersistenceAdapter implements TechnicianRepositoryPort {
    private final JpaTechnicianRepository repo;
    private final TechnicianMapper mapper;

    public TechnicianPersistenceAdapter(JpaTechnicianRepository repo, TechnicianMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override public Technician save(Technician t) { return mapper.toDomain(repo.save(mapper.toEntity(t))); }
    @Override public Optional<Technician> findById(Long id) { return repo.findById(id).map(mapper::toDomain); }
    @Override public Optional<Technician> findByDocument(String doc) { return repo.findByDocument(doc).map(mapper::toDomain); }
    @Override public boolean existsByDocument(String doc) { return repo.existsByDocument(doc); }
    @Override public List<Technician> findAll() { return repo.findAll().stream().map(mapper::toDomain).collect(Collectors.toList()); }
    @Override public List<Technician> findActiveBySpecialty(TicketCategory category) {
        return repo.findBySpecialtiesContainingAndStatus(category, UserStatus.ACTIVO)
            .stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
