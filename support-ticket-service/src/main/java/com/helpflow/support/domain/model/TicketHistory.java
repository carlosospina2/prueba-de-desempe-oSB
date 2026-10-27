package com.helpflow.support.domain.model;

import com.helpflow.support.domain.model.enums.TicketStatus;
import java.time.Instant;

public class TicketHistory {
    private Long id;
    private Long ticketId;
    private TicketStatus previousStatus;
    private TicketStatus newStatus;
    private String observation;
    private String changedBy;
    private Instant changedAt;

    public TicketHistory() {}

    public TicketHistory(Long ticketId, TicketStatus previousStatus, TicketStatus newStatus,
                         String observation, String changedBy) {
        this.ticketId = ticketId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.observation = observation;
        this.changedBy = changedBy;
        this.changedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
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
