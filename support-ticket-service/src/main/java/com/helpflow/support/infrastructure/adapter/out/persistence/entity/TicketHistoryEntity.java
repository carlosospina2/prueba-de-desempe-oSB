package com.helpflow.support.infrastructure.adapter.out.persistence.entity;

import com.helpflow.support.domain.model.enums.TicketStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ticket_history")
public class TicketHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketEntity ticket;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private TicketStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private TicketStatus newStatus;

    @Column(length = 500)
    private String observation;

    @Column(name = "changed_by", length = 150)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TicketEntity getTicket() { return ticket; }
    public void setTicket(TicketEntity ticket) { this.ticket = ticket; }
    public TicketStatus getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(TicketStatus previousStatus) { this.previousStatus = previousStatus; }
    public TicketStatus getNewStatus() { return newStatus; }
    public void setNewStatus(TicketStatus newStatus) { this.newStatus = newStatus; }
    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    public Instant getChangedAt() { return changedAt; }
    public void setChangedAt(Instant changedAt) { this.changedAt = changedAt; }
}
