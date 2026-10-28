package com.helpflow.support.infrastructure;

import com.helpflow.support.application.port.in.AssignTicketUseCase;
import com.helpflow.support.application.port.in.CreateTicketUseCase;
import com.helpflow.support.application.port.in.UpdateTicketStatusUseCase;
import com.helpflow.support.application.port.out.TicketRepositoryPort;
import com.helpflow.support.application.port.out.UserRepositoryPort;
import com.helpflow.support.domain.model.Ticket;
import com.helpflow.support.domain.model.User;
import com.helpflow.support.domain.model.enums.Role;
import com.helpflow.support.domain.model.enums.TicketStatus;
import com.helpflow.support.domain.model.enums.UserStatus;
import com.helpflow.support.infrastructure.adapter.in.rest.TicketController;
import com.helpflow.support.infrastructure.config.SecurityConfig;
import com.helpflow.support.infrastructure.security.JwtService;
import com.helpflow.support.infrastructure.security.UserDetailsServiceImpl;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security role tests for TicketController.
 *
 * Uses @WebMvcTest (web slice) + @WithMockUser to inject principals without JWT.
 * JwtService and UserDetailsServiceImpl are mocked because SecurityConfig requires
 * them but they are not part of the web slice autoconfiguration.
 * The "test" profile activates application-test.yml (H2 + Flyway disabled).
 *
 * Scenarios:
 *  - ROLE_USUARIO can GET /api/tickets/my                       → 200
 *  - ROLE_USUARIO cannot GET /api/tickets (admin-only)          → 403
 *  - ROLE_USUARIO can GET /api/tickets/{id} own ticket          → 200
 *  - ROLE_USUARIO gets 403 for another user's ticket            → 403
 *  - Anonymous GET /api/tickets/my                              → 401
 *  - Anonymous GET /api/tickets/{id}                            → 401
 *  - ROLE_ADMIN can GET /api/tickets                            → 200
 *  - ROLE_ADMIN can GET /api/tickets/{id} regardless of owner   → 200
 */
@WebMvcTest(TicketController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class SecurityRoleTest {

    @Autowired MockMvc mockMvc;

    // Security infrastructure beans required by SecurityConfig / JwtAuthenticationFilter
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    // Counters required by GlobalExceptionHandler
    @MockBean(name = "authFailuresCounter")  Counter authFailuresCounter;
    @MockBean(name = "accessDeniedCounter")  Counter accessDeniedCounter;

    // Application ports used by TicketController
    @MockBean TicketRepositoryPort ticketRepo;
    @MockBean UserRepositoryPort userRepo;
    @MockBean CreateTicketUseCase createTicketUseCase;
    @MockBean UpdateTicketStatusUseCase updateTicketStatusUseCase;
    @MockBean AssignTicketUseCase assignTicketUseCase;

    private static final Long OWNER_ID  = 10L;
    private static final Long OTHER_ID  = 20L;
    private static final Long TICKET_ID = 100L;

    @BeforeEach
    void setUp() {
        User owner = new User(OWNER_ID, "DOC10", "Owner", "owner@helpflow.com",
                "hashed", "Bogotá", "TI", UserStatus.ACTIVO, Role.ROLE_USUARIO);
        User other = new User(OTHER_ID, "DOC20", "Other", "other@helpflow.com",
                "hashed", "Cali", "TI", UserStatus.ACTIVO, Role.ROLE_USUARIO);
        User admin = new User(30L, "DOC30", "Admin", "admin@helpflow.com",
                "hashed", "Bogotá", "TI", UserStatus.ACTIVO, Role.ROLE_ADMIN);

        Ticket ownerTicket = buildTicket(TICKET_ID, OWNER_ID);

        when(userRepo.findByEmail("owner@helpflow.com")).thenReturn(Optional.of(owner));
        when(userRepo.findByEmail("other@helpflow.com")).thenReturn(Optional.of(other));
        when(userRepo.findByEmail("admin@helpflow.com")).thenReturn(Optional.of(admin));

        when(ticketRepo.findByUserId(OWNER_ID)).thenReturn(List.of(ownerTicket));
        when(ticketRepo.findByUserId(OTHER_ID)).thenReturn(List.of());
        when(ticketRepo.findAll()).thenReturn(List.of(ownerTicket));
        when(ticketRepo.findById(TICKET_ID)).thenReturn(Optional.of(ownerTicket));
    }

    // ─── GET /api/tickets/my ─────────────────────────────────────────────

    @Test
    @DisplayName("ROLE_USUARIO can GET /api/tickets/my → 200")
    @WithMockUser(username = "owner@helpflow.com", authorities = "ROLE_USUARIO")
    void usuarioCanAccessOwnTickets() throws Exception {
        mockMvc.perform(get("/api/tickets/my"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Anonymous GET /api/tickets/my → 401")
    @WithAnonymousUser
    void anonymousCannotAccessMyTickets() throws Exception {
        mockMvc.perform(get("/api/tickets/my"))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /api/tickets  (Admin only) ──────────────────────────────────

    @Test
    @DisplayName("ROLE_USUARIO cannot GET /api/tickets (admin-only) → 403")
    @WithMockUser(username = "owner@helpflow.com", authorities = "ROLE_USUARIO")
    void usuarioCannotAccessAllTickets() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ROLE_ADMIN can GET /api/tickets → 200")
    @WithMockUser(username = "admin@helpflow.com", authorities = "ROLE_ADMIN")
    void adminCanAccessAllTickets() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk());
    }

    // ─── GET /api/tickets/{id} (ownership enforcement) ───────────────────

    @Test
    @DisplayName("ROLE_USUARIO can GET /api/tickets/{id} own ticket → 200")
    @WithMockUser(username = "owner@helpflow.com", authorities = "ROLE_USUARIO")
    void usuarioCanAccessOwnTicketById() throws Exception {
        mockMvc.perform(get("/api/tickets/" + TICKET_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ROLE_USUARIO gets 403 requesting another user's ticket")
    @WithMockUser(username = "other@helpflow.com", authorities = "ROLE_USUARIO")
    void usuarioCannotAccessOtherUserTicket() throws Exception {
        mockMvc.perform(get("/api/tickets/" + TICKET_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ROLE_ADMIN can GET /api/tickets/{id} for any ticket → 200")
    @WithMockUser(username = "admin@helpflow.com", authorities = "ROLE_ADMIN")
    void adminCanAccessAnyTicketById() throws Exception {
        mockMvc.perform(get("/api/tickets/" + TICKET_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Anonymous GET /api/tickets/{id} → 401")
    @WithAnonymousUser
    void anonymousCannotAccessTicketById() throws Exception {
        mockMvc.perform(get("/api/tickets/" + TICKET_ID))
                .andExpect(status().isUnauthorized());
    }

    // ─── POST /api/tickets/{id}/assign (Admin only) ───────────────────────────

    @Test
    @DisplayName("ROLE_ADMIN can POST /api/tickets/{id}/assign → 200")
    @WithMockUser(username = "admin@helpflow.com", authorities = "ROLE_ADMIN")
    void adminCanAssignTicket() throws Exception {
        Ticket assignedTicket = buildTicket(TICKET_ID, OWNER_ID);
        assignedTicket.setStatus(TicketStatus.ASIGNADO);
        assignedTicket.setAssignedTechnicianId(1L);

        when(assignTicketUseCase.assign(TICKET_ID)).thenReturn(assignedTicket);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/tickets/" + TICKET_ID + "/assign"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ROLE_USUARIO cannot POST /api/tickets/{id}/assign → 403")
    @WithMockUser(username = "owner@helpflow.com", authorities = "ROLE_USUARIO")
    void usuarioCannotAssignTicket() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/tickets/" + TICKET_ID + "/assign"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ROLE_TECNICO cannot POST /api/tickets/{id}/assign → 403")
    @WithMockUser(username = "tecnico@helpflow.com", authorities = "ROLE_TECNICO")
    void tecnicoCannotAssignTicket() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/tickets/" + TICKET_ID + "/assign"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Anonymous POST /api/tickets/{id}/assign → 401")
    @WithAnonymousUser
    void anonymousCannotAssignTicket() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/tickets/" + TICKET_ID + "/assign"))
                .andExpect(status().isUnauthorized());
    }

    // ─── Helper ──────────────────────────────────────────────────────────

    private Ticket buildTicket(Long id, Long userId) {
        Ticket t = new Ticket();
        t.setId(id);
        t.setUserId(userId);
        t.setTitle("Test ticket");
        t.setDescription("Description");
        t.setStatus(TicketStatus.ABIERTO);
        t.setCreatedAt(Instant.now());
        return t;
    }
}
