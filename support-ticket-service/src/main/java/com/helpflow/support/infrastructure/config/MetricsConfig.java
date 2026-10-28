package com.helpflow.support.infrastructure.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application-level metrics for HelpFlow.
 *
 * Tags are kept low-cardinality (category, priority, status) to avoid
 * cardinality explosion. Never use ticket IDs or user IDs as tag values.
 */
@Configuration
public class MetricsConfig {

    // ── Security ────────────────────────────────────────────────────────

    @Bean
    public Counter authFailuresCounter(MeterRegistry registry) {
        return Counter.builder("helpflow.auth.failures")
                .description("Number of failed authentication attempts")
                .register(registry);
    }

    @Bean
    public Counter accessDeniedCounter(MeterRegistry registry) {
        return Counter.builder("helpflow.access.denied")
                .description("Number of access-denied responses returned")
                .register(registry);
    }

    // ── Tickets ─────────────────────────────────────────────────────────

    /**
     * Tickets created.
     * Usage: inject Counter and call increment() in CreateTicketService.
     * Tag "category" → HARDWARE, SOFTWARE, RED, ACCESOS, OTRO
     */
    @Bean
    public Counter ticketsCreatedCounter(MeterRegistry registry) {
        return Counter.builder("helpflow.tickets.created")
                .description("Total tickets created")
                .register(registry);
    }

    /**
     * Tickets escalated automatically by the SLA scheduler.
     */
    @Bean
    public Counter ticketsEscalatedCounter(MeterRegistry registry) {
        return Counter.builder("helpflow.tickets.escalated")
                .description("Total tickets auto-escalated due to SLA proximity")
                .register(registry);
    }

    /**
     * Tickets resolved (reached RESUELTO status).
     */
    @Bean
    public Counter ticketsResolvedCounter(MeterRegistry registry) {
        return Counter.builder("helpflow.tickets.resolved")
                .description("Total tickets resolved")
                .register(registry);
    }

    /**
     * Dispatch calls made to technician-dispatch-service.
     * Tag "result" → success / failure
     */
    @Bean
    public Counter dispatchCallsCounter(MeterRegistry registry) {
        return Counter.builder("helpflow.dispatch.calls")
                .description("Total calls to the technician dispatch service")
                .register(registry);
    }
}
