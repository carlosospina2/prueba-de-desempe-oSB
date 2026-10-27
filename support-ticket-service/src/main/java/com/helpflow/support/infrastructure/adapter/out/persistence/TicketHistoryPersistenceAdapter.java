package com.helpflow.support.infrastructure.adapter.out.persistence;

import com.helpflow.support.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.support.domain.model.TicketHistory;
import com.helpflow.support.infrastructure.adapter.out.persistence.entity.TicketHistoryEntity;
import com.helpflow.support.infrastructure.adapter.out.persistence.mapper.TicketHistoryMapper;
import com.helpflow.support.infrastructure.adapter.out.persistence.repository.JpaTicketHistoryRepository;
import com.helpflow.support.infrastructure.adapter.out.persistence.repository.JpaTicketRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TicketHistoryPersistenceAdapter implements TicketHistoryRepositoryPort {
    private final JpaTicketHistoryRepository repo;
    private final JpaTicketRepository ticketRepo;
    private final TicketHistoryMapper mapper;

    public TicketHistoryPersistenceAdapter(JpaTicketHistoryRepository repo,
                                           JpaTicketRepository ticketRepo,
                                           TicketHistoryMapper mapper) {
        this.repo = repo;
        this.ticketRepo = ticketRepo;
        this.mapper = mapper;
    }

    @Override
    public TicketHistory save(TicketHistory history) {
        TicketHistoryEntity entity = mapper.toEntity(history);
        if (history.getTicketId() != null) {
            ticketRepo.findById(history.getTicketId()).ifPresent(entity::setTicket);
        }
        entity.setChangedAt(history.getChangedAt() != null ? history.getChangedAt() : java.time.Instant.now());
        return mapper.toDomain(repo.save(entity));
    }

    @Override
    public List<TicketHistory> findByTicketId(Long ticketId) {
        return repo.findByTicketId(ticketId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
