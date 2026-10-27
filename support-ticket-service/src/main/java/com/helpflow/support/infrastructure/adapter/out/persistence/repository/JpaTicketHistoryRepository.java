package com.helpflow.support.infrastructure.adapter.out.persistence.repository;

import com.helpflow.support.infrastructure.adapter.out.persistence.entity.TicketHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JpaTicketHistoryRepository extends JpaRepository<TicketHistoryEntity, Long> {
    List<TicketHistoryEntity> findByTicketId(Long ticketId);
}
