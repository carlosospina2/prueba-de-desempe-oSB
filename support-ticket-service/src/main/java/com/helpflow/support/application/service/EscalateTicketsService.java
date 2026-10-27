package com.helpflow.support.application.service;

import com.helpflow.support.application.port.in.EscalateTicketsUseCase;
import com.helpflow.support.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.support.application.port.out.TicketRepositoryPort;
import com.helpflow.support.domain.model.TicketHistory;
import com.helpflow.support.domain.model.enums.TicketPriority;
import com.helpflow.support.infrastructure.config.SlaConfig;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EscalateTicketsService implements EscalateTicketsUseCase {

    private final TicketRepositoryPort ticketRepo;
    private final TicketHistoryRepositoryPort historyRepo;
    private final SlaConfig slaConfig;

    public EscalateTicketsService(TicketRepositoryPort ticketRepo,
                                  TicketHistoryRepositoryPort historyRepo,
                                  SlaConfig slaConfig) {
        this.ticketRepo = ticketRepo;
        this.historyRepo = historyRepo;
        this.slaConfig = slaConfig;
    }

    @Override
    @Transactional
    @Scheduled(fixedDelayString = "${helpflow.sla.escalation-check-ms:60000}")
    public int escalateNearDeadline() {
        var tickets = ticketRepo.findNearSlaDeadline(slaConfig.getEscalationThresholdMinutes());
        int count = 0;
        for (var ticket : tickets) {
            if (ticket.getPriority() != TicketPriority.CRÍTICA) {
                TicketPriority prev = ticket.getPriority();
                TicketPriority escalated = escalate(prev);
                ticket.setPriority(escalated);
                ticketRepo.save(ticket);
                historyRepo.save(new TicketHistory(ticket.getId(), ticket.getStatus(), ticket.getStatus(),
                    "Auto-escalated from " + prev + " to " + escalated + " due to SLA", "SYSTEM"));
                count++;
            }
        }
        return count;
    }

    private TicketPriority escalate(TicketPriority priority) {
        return switch (priority) {
            case BAJA -> TicketPriority.MEDIA;
            case MEDIA -> TicketPriority.ALTA;
            case ALTA -> TicketPriority.CRÍTICA;
            default -> priority;
        };
    }
}
