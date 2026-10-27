package com.helpflow.support.infrastructure.adapter.in.rest;

import com.helpflow.support.application.port.in.CreateTicketUseCase;
import com.helpflow.support.application.port.in.UpdateTicketStatusUseCase;
import com.helpflow.support.application.port.out.TicketRepositoryPort;
import com.helpflow.support.application.port.out.UserRepositoryPort;
import com.helpflow.support.domain.exception.TicketNotFoundException;
import com.helpflow.support.domain.exception.UserNotFoundException;
import com.helpflow.support.domain.model.Ticket;
import com.helpflow.support.domain.model.User;
import com.helpflow.support.domain.model.enums.TicketCategory;
import com.helpflow.support.domain.model.enums.TicketPriority;
import com.helpflow.support.domain.model.enums.TicketStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Tickets", description = "Support ticket management")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

    private final CreateTicketUseCase createTicket;
    private final UpdateTicketStatusUseCase updateStatus;
    private final TicketRepositoryPort ticketRepo;
    private final UserRepositoryPort userRepo;

    public TicketController(CreateTicketUseCase createTicket,
                            UpdateTicketStatusUseCase updateStatus,
                            TicketRepositoryPort ticketRepo,
                            UserRepositoryPort userRepo) {
        this.createTicket = createTicket;
        this.updateStatus = updateStatus;
        this.ticketRepo = ticketRepo;
        this.userRepo = userRepo;
    }

    public record CreateTicketRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull TicketCategory category,
        @NotNull TicketPriority priority,
        @NotBlank String sede,
        String affectedAsset
    ) {}

    public record UpdateStatusRequest(@NotNull TicketStatus newStatus, String observation) {}

    @PostMapping
    @Operation(summary = "Create a new support ticket")
    @PreAuthorize("hasAnyRole('USUARIO','ADMIN')")
    public ResponseEntity<Ticket> create(@Valid @RequestBody CreateTicketRequest req,
                                          @AuthenticationPrincipal UserDetails principal) {
        User user = userRepo.findByEmail(principal.getUsername())
            .orElseThrow(() -> new UserNotFoundException(principal.getUsername()));
        Ticket ticket = createTicket.create(user.getId(), req.title(), req.description(),
            req.category(), req.priority(), req.sede(), req.affectedAsset());
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @GetMapping("/my")
    @Operation(summary = "Get my tickets")
    @PreAuthorize("hasAnyRole('USUARIO','ADMIN')")
    public ResponseEntity<List<Ticket>> myTickets(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepo.findByEmail(principal.getUsername())
            .orElseThrow(() -> new UserNotFoundException(principal.getUsername()));
        return ResponseEntity.ok(ticketRepo.findByUserId(user.getId()));
    }

    @GetMapping
    @Operation(summary = "Get all tickets (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Ticket>> all() {
        return ResponseEntity.ok(ticketRepo.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by ID")
    public ResponseEntity<Ticket> getById(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails principal) {
        Ticket ticket = ticketRepo.findById(id)
            .orElseThrow(() -> new TicketNotFoundException(id));
        return ResponseEntity.ok(ticket);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update ticket status")
    @PreAuthorize("hasAnyRole('TECNICO','ADMIN')")
    public ResponseEntity<Ticket> updateStatus(@PathVariable Long id,
                                                @Valid @RequestBody UpdateStatusRequest req,
                                                @AuthenticationPrincipal UserDetails principal) {
        Ticket ticket = updateStatus.updateStatus(id, req.newStatus(), req.observation(), principal.getUsername());
        return ResponseEntity.ok(ticket);
    }
}
