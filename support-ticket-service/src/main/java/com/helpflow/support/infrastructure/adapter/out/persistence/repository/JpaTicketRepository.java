package com.helpflow.support.infrastructure.adapter.out.persistence.repository;

import com.helpflow.support.infrastructure.adapter.out.persistence.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface JpaTicketRepository extends JpaRepository<TicketEntity, Long> {
    List<TicketEntity> findByUserId(Long userId);
    List<TicketEntity> findByTechnicianId(Long technicianId);

    @Query("SELECT t FROM TicketEntity t WHERE t.technician.id = :techId AND t.status NOT IN ('RESUELTO','CERRADO','CANCELADO')")
    List<TicketEntity> findActiveByTechnicianId(@Param("techId") Long technicianId);

    @Query("SELECT COUNT(t) FROM TicketEntity t WHERE t.technician.id = :techId AND t.status NOT IN ('RESUELTO','CERRADO','CANCELADO')")
    int countActiveByTechnicianId(@Param("techId") Long technicianId);

    @Query("SELECT t FROM TicketEntity t WHERE t.slaDeadline < :threshold AND t.status NOT IN ('RESUELTO','CERRADO','CANCELADO')")
    List<TicketEntity> findNearSlaDeadline(@Param("threshold") Instant threshold);
}
