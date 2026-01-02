package com.finance.concierge.config;

import com.finance.concierge.FinanceAgent;
import com.google.adk.runner.InMemoryRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Agent-related beans
 * Following Dependency Inversion Principle (DIP)
 */
@Slf4j
@Configuration
public class AgentConfig {

    @Value("${google.api.key}")
    private String googleApiKey;

    /**
     * Create InMemoryRunner bean for dependency injection
     */
    @Bean
    public InMemoryRunner inMemoryRunner() {
        log.info("Initializing InMemoryRunner with Finance Agent");

        if (googleApiKey == null || googleApiKey.isBlank() ||
            googleApiKey.equals("GOOGLE_API_KEY_PLACEHOLDER")) {
            log.warn("Google API Key is not configured properly. Please set it in application.properties");
        }

        return new InMemoryRunner(FinanceAgent.createAgent(googleApiKey));
    }
}

