package com.helpflow.support.infrastructure.adapter.out.persistence.entity;

import com.helpflow.support.domain.model.enums.TicketCategory;
import com.helpflow.support.domain.model.enums.TicketPriority;
import com.helpflow.support.domain.model.enums.TicketStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "tickets")
@NamedEntityGraph(name = "Ticket.withHistory",
    attributeNodes = @NamedAttributeNode("history"))
public class TicketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TicketPriority priority;

    @Column(nullable = false, length = 100)
    private String sede;

    @Column(name = "affected_asset", length = 200)
    private String affectedAsset;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "sla_deadline")
    private Instant slaDeadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private TechnicianEntity technician;

    @OneToMany(mappedBy = "ticket", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TicketHistoryEntity> history;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
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
    public TechnicianEntity getTechnician() { return technician; }
    public void setTechnician(TechnicianEntity technician) { this.technician = technician; }
    public List<TicketHistoryEntity> getHistory() { return history; }
    public void setHistory(List<TicketHistoryEntity> history) { this.history = history; }
}
