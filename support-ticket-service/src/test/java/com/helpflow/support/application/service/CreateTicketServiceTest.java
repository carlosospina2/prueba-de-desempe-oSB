package com.helpflow.support.application.service;

import com.helpflow.support.application.port.out.*;
import com.helpflow.support.domain.exception.BusinessRuleException;
import com.helpflow.support.domain.exception.UserNotFoundException;
import com.helpflow.support.domain.model.*;
import com.helpflow.support.domain.model.enums.*;
import com.helpflow.support.infrastructure.config.SlaConfig;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CreateTicketService.
 * Uses LENIENT stubbing to allow @BeforeEach stubs that are not needed by
 * every test (e.g. SLA config is not reached when the user is inactive or missing).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateTicketServiceTest {

    @Mock TicketRepositoryPort ticketRepo;
    @Mock UserRepositoryPort userRepo;
    @Mock TechnicianRepositoryPort technicianRepo;
    @Mock TicketHistoryRepositoryPort historyRepo;
    @Mock TechnicianDispatchPort dispatchPort;
    @Mock SlaConfig slaConfig;
    @Mock Counter ticketsCreatedCounter;
    @Mock Counter dispatchCallsCounter;

    private CreateTicketService service;

    @BeforeEach
    void setUp() {
        service = new CreateTicketService(ticketRepo, userRepo, technicianRepo, historyRepo,
                dispatchPort, slaConfig, ticketsCreatedCounter, dispatchCallsCounter);
        // SLA config is needed by the happy path. Lenient mode prevents failures
        // when other tests exit early (inactive user, user not found).
        when(slaConfig.getHoursPerPriority()).thenReturn(Map.of("ALTA", 8, "BAJA", 72, "MEDIA", 24));
        when(slaConfig.getMaxActiveTicketsPerTechnician()).thenReturn(5);
    }

    @Test
    void shouldCreateTicketForActiveUser() {
        User user = new User(1L, "DOC1", "Ana", "ana@test.com", "pass",
                "Bogotá", "TI", UserStatus.ACTIVO, Role.ROLE_USUARIO);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(ticketRepo.save(any())).thenAnswer(i -> {
            Ticket t = i.getArgument(0);
            t.setId(1L);
            return t;
        });
        when(historyRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(technicianRepo.findActiveBySpecialty(any())).thenReturn(List.of());

        Ticket result = service.create(1L, "PC rota", "La PC no enciende",
                TicketCategory.HARDWARE, TicketPriority.ALTA, "Bogotá", "PC-001");

        assertNotNull(result);
        assertEquals(TicketStatus.ABIERTO, result.getStatus());
        assertEquals(TicketCategory.HARDWARE, result.getCategory());
        verify(ticketRepo).save(any());
        verify(historyRepo).save(any());
    }

    @Test
    void shouldRejectTicketForInactiveUser() {
        User user = new User(2L, "DOC2", "Bob", "bob@test.com", "pass",
                "Cali", "TI", UserStatus.INACTIVO, Role.ROLE_USUARIO);
        when(userRepo.findById(2L)).thenReturn(Optional.of(user));

        assertThrows(BusinessRuleException.class, () ->
                service.create(2L, "Titulo", "Desc", TicketCategory.SOFTWARE,
                        TicketPriority.BAJA, "Cali", "APP-001"));
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                service.create(99L, "T", "D", TicketCategory.RED,
                        TicketPriority.MEDIA, "Sede", "Asset"));
    }
}
