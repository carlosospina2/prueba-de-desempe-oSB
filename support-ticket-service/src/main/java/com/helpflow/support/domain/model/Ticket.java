package com.helpflow.support.domain.model;

import com.helpflow.support.domain.model.enums.TicketCategory;
import com.helpflow.support.domain.model.enums.TicketPriority;
import com.helpflow.support.domain.model.enums.TicketStatus;
import com.helpflow.support.domain.exception.InvalidTicketTransitionException;
import java.time.Instant;
import java.util.Set;
import java.util.EnumSet;
import java.util.Map;

public class Ticket {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private TicketCategory category;
    private TicketPriority priority;
    private String sede;
    private String affectedAsset;
    private Instant createdAt;
    private Instant slaDeadline;
    private TicketStatus status;
    private Long assignedTechnicianId;

    private static final Map<TicketStatus, Set<TicketStatus>> VALID_TRANSITIONS = Map.of(
        TicketStatus.ABIERTO,     EnumSet.of(TicketStatus.ASIGNADO, TicketStatus.CANCELADO),
        TicketStatus.ASIGNADO,    EnumSet.of(TicketStatus.EN_PROGRESO, TicketStatus.CANCELADO),
        TicketStatus.EN_PROGRESO, EnumSet.of(TicketStatus.RESUELTO, TicketStatus.CANCELADO),
        TicketStatus.RESUELTO,    EnumSet.of(TicketStatus.CERRADO),
        TicketStatus.CERRADO,     EnumSet.noneOf(TicketStatus.class),
        TicketStatus.CANCELADO,   EnumSet.noneOf(TicketStatus.class)
    );

    public void transitionTo(TicketStatus newStatus) {
        Set<TicketStatus> allowed = VALID_TRANSITIONS.getOrDefault(this.status, EnumSet.noneOf(TicketStatus.class));
        if (!allowed.contains(newStatus)) {
            throw new InvalidTicketTransitionException(
                "Cannot transition from " + this.status + " to " + newStatus);
        }
        this.status = newStatus;
    }

    public boolean isNearSlaDeadline(int thresholdMinutes) {
        if (slaDeadline == null) return false;
        Instant threshold = Instant.now().plusSeconds(thresholdMinutes * 60L);
        return slaDeadline.isBefore(threshold) && !isTerminal();
    }

    public boolean isTerminal() {
        return status == TicketStatus.CERRADO || status == TicketStatus.CANCELADO || status == TicketStatus.RESUELTO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TicketCategory getCategory() { return category; }
    public void setCategory(TicketCategory category) { this.category = category; }
    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }
    public String getSede() { return sede; }
    public void setSede(String sede) { this.sede = sede; }
    public String getAffectedAsset() { return affectedAsset; }
    public void setAffectedAsset(String affectedAsset) { this.affectedAsset = affectedAsset; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getSlaDeadline() { return slaDeadline; }
    public void setSlaDeadline(Instant slaDeadline) { this.slaDeadline = slaDeadline; }
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public Long getAssignedTechnicianId() { return assignedTechnicianId; }
    public void setAssignedTechnicianId(Long assignedTechnicianId) { this.assignedTechnicianId = assignedTechnicianId; }
}
