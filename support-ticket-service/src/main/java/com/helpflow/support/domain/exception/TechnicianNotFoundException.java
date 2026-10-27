package com.helpflow.support.domain.exception;

public class TechnicianNotFoundException extends DomainException {
    public TechnicianNotFoundException(String identifier) {
        super("Technician not found: " + identifier);
    }
}
