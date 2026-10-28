package com.helpflow.support.application.service;

import com.helpflow.support.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.support.application.port.out.TicketRepositoryPort;
import com.helpflow.support.domain.model.Ticket;
import com.helpflow.support.domain.model.enums.TicketPriority;
import com.helpflow.support.domain.model.enums.TicketStatus;
import com.helpflow.support.infrastructure.config.SlaConfig;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EscalateTicketsService.
 *
 * Verifies:
 *  - BAJA ticket near SLA deadline is escalated to MEDIA
 *  - MEDIA ticket near SLA deadline is escalated to ALTA
 *  - ALTA ticket near SLA deadline is escalated to CRÍTICA
 *  - CRÍTICA ticket is NOT escalated (already maximum priority)
 *  - Counter is incremented once per escalated ticket
 *  - Ticket and history are saved for each escalated ticket
 *  - No escalation occurs when no tickets are near deadline
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EscalateTicketsServiceTest {

    @Mock TicketRepositoryPort ticketRepo;
    @Mock TicketHistoryRepositoryPort historyRepo;
    @Mock SlaConfig slaConfig;
    @Mock Counter ticketsEscalatedCounter;

    private EscalateTicketsService service;

    @BeforeEach
    void setUp() {
        service = new EscalateTicketsService(ticketRepo, historyRepo, slaConfig, ticketsEscalatedCounter);
        when(slaConfig.getEscalationThresholdMinutes()).thenReturn(60);
        when(ticketRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(historyRepo.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    @DisplayName("BAJA ticket is escalated to MEDIA and counter is incremented")
    void escalatesBajaToMedia() {
        Ticket ticket = buildTicket(1L, TicketPriority.BAJA);
        when(ticketRepo.findNearSlaDeadline(60)).thenReturn(List.of(ticket));

        int count = service.escalateNearDeadline();

        assertEquals(1, count);
        assertEquals(TicketPriority.MEDIA, ticket.getPriority());
        verify(ticketRepo).save(ticket);
        verify(historyRepo).save(any());
        verify(ticketsEscalatedCounter).increment();
    }

    @Test
    @DisplayName("MEDIA ticket is escalated to ALTA")
    void escalatesMediaToAlta() {
        Ticket ticket = buildTicket(2L, TicketPriority.MEDIA);
        when(ticketRepo.findNearSlaDeadline(60)).thenReturn(List.of(ticket));

        int count = service.escalateNearDeadline();

        assertEquals(1, count);
        assertEquals(TicketPriority.ALTA, ticket.getPriority());
        verify(ticketsEscalatedCounter).increment();
    }

    @Test
    @DisplayName("ALTA ticket is escalated to CRÍTICA")
    void escalatesAltaToCritica() {
        Ticket ticket = buildTicket(3L, TicketPriority.ALTA);
        when(ticketRepo.findNearSlaDeadline(60)).thenReturn(List.of(ticket));

        int count = service.escalateNearDeadline();

        assertEquals(1, count);
        assertEquals(TicketPriority.CRÍTICA, ticket.getPriority());
        verify(ticketsEscalatedCounter).increment();
    }

    @Test
    @DisplayName("CRÍTICA ticket is NOT escalated — counter NOT incremented")
    void doesNotEscalateCritica() {
        Ticket ticket = buildTicket(4L, TicketPriority.CRÍTICA);
        when(ticketRepo.findNearSlaDeadline(60)).thenReturn(List.of(ticket));

        int count = service.escalateNearDeadline();

        assertEquals(0, count);
        assertEquals(TicketPriority.CRÍTICA, ticket.getPriority());
        verify(ticketRepo, never()).save(any());
        verify(ticketsEscalatedCounter, never()).increment();
    }

    @Test
    @DisplayName("Mixed batch: 2 escalated, 1 CRÍTICA skipped — counter incremented twice")
    void escalatesMixedBatch() {
        Ticket baja = buildTicket(10L, TicketPriority.BAJA);
        Ticket alta = buildTicket(11L, TicketPriority.ALTA);
        Ticket critica = buildTicket(12L, TicketPriority.CRÍTICA);
        when(ticketRepo.findNearSlaDeadline(60)).thenReturn(List.of(baja, alta, critica));

        int count = service.escalateNearDeadline();

        assertEquals(2, count);
        assertEquals(TicketPriority.MEDIA,   baja.getPriority());
        assertEquals(TicketPriority.CRÍTICA, alta.getPriority());
        assertEquals(TicketPriority.CRÍTICA, critica.getPriority()); // unchanged
        verify(ticketsEscalatedCounter, times(2)).increment();
    }

    @Test
    @DisplayName("No tickets near deadline — returns 0, counter not incremented")
    void noTicketsNearDeadline() {
        when(ticketRepo.findNearSlaDeadline(60)).thenReturn(List.of());

        int count = service.escalateNearDeadline();

        assertEquals(0, count);
        verify(ticketRepo, never()).save(any());
        verify(ticketsEscalatedCounter, never()).increment();
    }

    // ─── Helper ──────────────────────────────────────────────────────────

    private Ticket buildTicket(Long id, TicketPriority priority) {
        Ticket t = new Ticket();
        t.setId(id);
        t.setPriority(priority);
        t.setStatus(TicketStatus.ASIGNADO);
        t.setSlaDeadline(Instant.now().plusSeconds(30 * 60)); // 30 min from now
        return t;
    }
}
