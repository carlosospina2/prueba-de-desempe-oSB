package com.helpflow.support;

import com.helpflow.support.infrastructure.config.SlaConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(SlaConfig.class)
public class SupportTicketApplication {
    public static void main(String[] args) {
        SpringApplication.run(SupportTicketApplication.class, args);
    }
}
