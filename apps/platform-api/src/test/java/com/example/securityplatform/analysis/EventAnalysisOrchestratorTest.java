package com.example.securityplatform.analysis;

import com.example.securityplatform.events.domain.RawIngestedEvent;
import com.example.securityplatform.events.domain.SourceType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventAnalysisOrchestratorTest {

    private final EventAnalysisOrchestrator orchestrator =
            new EventAnalysisOrchestrator(new TrafficAnalysisService(), new LogAnalysisService());

    @Test
    void analyzeCombinesTrafficAndLogSignalsIntoSingleContext() {
        var rawEvent = new RawIngestedEvent(
                UUID.randomUUID(),
                SourceType.SIMULATED_ATTACK,
                Instant.parse("2026-04-24T10:00:00Z"),
                "185.24.91.18",
                "10.0.0.10",
                22,
                "admin",
                "admin-gateway",
                "/admin/login",
                "port scan detected with brute force attempt and blocked ip from threat intel",
                null,
                Instant.now()
        );

        var context = orchestrator.analyze(rawEvent);

        assertThat(context.scanLikeTraffic()).isTrue();
        assertThat(context.bruteForceLike()).isTrue();
        assertThat(context.blacklistIndicator()).isTrue();
        assertThat(context.adminTargeting()).isTrue();
        assertThat(context.findings()).isNotEmpty();
    }
}
