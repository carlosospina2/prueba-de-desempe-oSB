package com.helpflow.support.infrastructure.adapter.out.persistence.entity;

import com.helpflow.support.domain.model.enums.TicketCategory;
import com.helpflow.support.domain.model.enums.UserStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "technicians")
public class TechnicianEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String document;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal salary;

    @Column(name = "affiliation_date", nullable = false)
    private LocalDate affiliationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserStatus status;

    @Column(nullable = false, length = 100)
    private String sede;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "technician_specialties", joinColumns = @JoinColumn(name = "technician_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "specialty")
    private Set<TicketCategory> specialties;

    @Column(name = "support_profile", length = 500)
    private String supportProfile;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    public LocalDate getAffiliationDate() { return affiliationDate; }
    public void setAffiliationDate(LocalDate affiliationDate) { this.affiliationDate = affiliationDate; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    public String getSede() { return sede; }
    public void setSede(String sede) { this.sede = sede; }
    public Set<TicketCategory> getSpecialties() { return specialties; }
    public void setSpecialties(Set<TicketCategory> specialties) { this.specialties = specialties; }
    public String getSupportProfile() { return supportProfile; }
    public void setSupportProfile(String supportProfile) { this.supportProfile = supportProfile; }
}
