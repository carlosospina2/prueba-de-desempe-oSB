package com.helpflow.support.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "helpflow.sla")
public class SlaConfig {

    /**
     * SLA hours per priority. Decision: BAJA=72h, MEDIA=24h, ALTA=8h, CRÍTICA=2h.
     * These are implementation defaults; admin can reconfigure via application properties.
     */
    private Map<String, Integer> hoursPerPriority = Map.of(
        "BAJA", 72,
        "MEDIA", 24,
        "ALTA", 8,
        "CRÍTICA", 2
    );

    private int escalationThresholdMinutes = 60;
    private int maxActiveTicketsPerTechnician = 5;

    public Map<String, Integer> getHoursPerPriority() { return hoursPerPriority; }
    public void setHoursPerPriority(Map<String, Integer> hoursPerPriority) { this.hoursPerPriority = hoursPerPriority; }
    public int getEscalationThresholdMinutes() { return escalationThresholdMinutes; }
    public void setEscalationThresholdMinutes(int escalationThresholdMinutes) { this.escalationThresholdMinutes = escalationThresholdMinutes; }
    public int getMaxActiveTicketsPerTechnician() { return maxActiveTicketsPerTechnician; }
    public void setMaxActiveTicketsPerTechnician(int maxActiveTicketsPerTechnician) { this.maxActiveTicketsPerTechnician = maxActiveTicketsPerTechnician; }
}
