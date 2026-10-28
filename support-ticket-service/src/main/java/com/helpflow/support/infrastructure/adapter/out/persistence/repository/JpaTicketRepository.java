package com.helpflow.support.infrastructure.adapter.out.persistence.repository;

import com.helpflow.support.infrastructure.adapter.out.persistence.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface JpaTicketRepository extends JpaRepository<TicketEntity, Long> {

    // Use JOIN FETCH to avoid N+1 when mapping user.id and technician.id
    @Query("SELECT t FROM TicketEntity t LEFT JOIN FETCH t.user LEFT JOIN FETCH t.technician WHERE t.user.id = :userId")
    List<TicketEntity> findByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM TicketEntity t LEFT JOIN FETCH t.user LEFT JOIN FETCH t.technician WHERE t.technician.id = :techId")
    List<TicketEntity> findByTechnicianId(@Param("techId") Long techId);

    @Query("SELECT t FROM TicketEntity t WHERE t.technician.id = :techId AND t.status NOT IN ('RESUELTO','CERRADO','CANCELADO')")
    List<TicketEntity> findActiveByTechnicianId(@Param("techId") Long technicianId);

    @Query("SELECT COUNT(t) FROM TicketEntity t WHERE t.technician.id = :techId AND t.status NOT IN ('RESUELTO','CERRADO','CANCELADO')")
    int countActiveByTechnicianId(@Param("techId") Long technicianId);

    @Query("SELECT t FROM TicketEntity t LEFT JOIN FETCH t.user LEFT JOIN FETCH t.technician WHERE t.slaDeadline < :threshold AND t.status NOT IN ('RESUELTO','CERRADO','CANCELADO')")
    List<TicketEntity> findNearSlaDeadline(@Param("threshold") Instant threshold);

    // Override findAll to eagerly fetch associations and avoid N+1
    @Query("SELECT t FROM TicketEntity t LEFT JOIN FETCH t.user LEFT JOIN FETCH t.technician")
    List<TicketEntity> findAll();
}
