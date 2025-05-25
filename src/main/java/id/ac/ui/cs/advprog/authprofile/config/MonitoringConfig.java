package id.ac.ui.cs.advprog.authprofile.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.Info;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class MonitoringConfig {

    public MeterRegistry meterRegistry; // Make public for access from services

    // Authentication metrics
    @Getter
    private final Counter loginAttempts;
    @Getter
    private final Counter loginSuccessful;
    @Getter
    private final Counter loginFailed;
    @Getter
    private final Counter registrationAttempts;
    @Getter
    private final Counter profileUpdates;
    @Getter
    private final Counter authorizationDenied;

    // Search-specific metrics
    @Getter
    private final Counter searchRequests;
    @Getter
    private final Counter searchResults;
    @Getter
    private final Counter suggestionRequests;
    @Getter
    private final Counter suggestionResults;

    // Gauge for active sessions (simplified)
    @Getter
    private final AtomicInteger activeSessions = new AtomicInteger(0);

    @Autowired
    public MonitoringConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize authentication metrics
        this.loginAttempts = Counter.builder("auth_login_attempts_total")
                .description("Total login attempts")
                .register(meterRegistry);

        this.loginSuccessful = Counter.builder("auth_login_successful_total")
                .description("Successful login attempts")
                .register(meterRegistry);

        this.loginFailed = Counter.builder("auth_login_failed_total")
                .description("Failed login attempts")
                .register(meterRegistry);

        this.registrationAttempts = Counter.builder("auth_registration_attempts_total")
                .description("Total registration attempts")
                .register(meterRegistry);

        this.profileUpdates = Counter.builder("profile_updates_total")
                .description("Profile update operations")
                .register(meterRegistry);

        this.authorizationDenied = Counter.builder("auth_authorization_denied_total")
                .description("Authorization denied events")
                .register(meterRegistry);

        // Initialize search-specific metrics
        this.searchRequests = Counter.builder("search_requests_total")
                .description("Total search operations performed")
                .tag("service", "caregiver")
                .register(meterRegistry);

        this.searchResults = Counter.builder("search_caregivers_results_total")
                .description("Total search results returned")
                .register(meterRegistry);

        this.suggestionRequests = Counter.builder("search_suggestions_requests_total")
                .description("Total suggestion requests")
                .register(meterRegistry);

        this.suggestionResults = Counter.builder("search_suggestions_results_total")
                .description("Total suggestion results returned")
                .register(meterRegistry);

        // Register gauge for active sessions using the correct API
        meterRegistry.gauge("auth_sessions_active", Tags.empty(), activeSessions, AtomicInteger::get);
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }


}
