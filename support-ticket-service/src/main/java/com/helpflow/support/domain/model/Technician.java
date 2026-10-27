package com.helpflow.support.domain.model;

import com.helpflow.support.domain.model.enums.TicketCategory;
import com.helpflow.support.domain.model.enums.UserStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public class Technician {
    private Long id;
    private String document;
    private String name;
    private BigDecimal salary;
    private LocalDate affiliationDate;
    private UserStatus status;
    private String sede;
    private Set<TicketCategory> specialties;
    private String supportProfile;

    public Technician() {}

    public boolean isActive() {
        return UserStatus.ACTIVO.equals(this.status);
    }

    public boolean hasSpecialty(TicketCategory category) {
        return specialties != null && specialties.contains(category);
    }

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
