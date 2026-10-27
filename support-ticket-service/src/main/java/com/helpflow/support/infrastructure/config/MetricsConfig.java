package com.helpflow.support.infrastructure.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter authFailuresCounter(MeterRegistry registry) {
        return Counter.builder("helpflow.auth.failures")
            .description("Number of authentication failures")
            .register(registry);
    }

    @Bean
    public Counter accessDeniedCounter(MeterRegistry registry) {
        return Counter.builder("helpflow.access.denied")
            .description("Number of access denied events")
            .register(registry);
    }
}
