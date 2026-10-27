package com.helpflow.support.infrastructure.adapter.in.rest;

import com.helpflow.support.application.port.out.TechnicianRepositoryPort;
import com.helpflow.support.domain.exception.TechnicianNotFoundException;
import com.helpflow.support.domain.model.Technician;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/technicians")
@Tag(name = "Technicians", description = "Technician management")
@SecurityRequirement(name = "bearerAuth")
public class TechnicianController {

    private final TechnicianRepositoryPort technicianRepo;

    public TechnicianController(TechnicianRepositoryPort technicianRepo) {
        this.technicianRepo = technicianRepo;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all technicians")
    public ResponseEntity<List<Technician>> all() {
        return ResponseEntity.ok(technicianRepo.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    @Operation(summary = "Get technician by ID")
    public ResponseEntity<Technician> getById(@PathVariable Long id) {
        return technicianRepo.findById(id).map(ResponseEntity::ok)
            .orElseThrow(() -> new TechnicianNotFoundException(String.valueOf(id)));
    }
}
