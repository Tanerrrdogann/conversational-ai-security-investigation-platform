package com.example.securityplatform.anomaly;

import com.example.securityplatform.anomaly.service.FeatureExtractionService;
import com.example.securityplatform.common.Severity;
import com.example.securityplatform.events.domain.NormalizedEvent;
import com.example.securityplatform.events.domain.EventType;
import com.example.securityplatform.events.domain.SourceType;
import com.example.securityplatform.events.dto.NormalizedEventResponse;
import com.example.securityplatform.events.repository.NormalizedEventRepository;
import com.example.securityplatform.rules.service.RuleEvaluationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureExtractionServiceTest {

    @Mock
    private NormalizedEventRepository normalizedEventRepository;

    @Test
    void extractBuildsExpectedFeatureVector() {
        var featureExtractionService = new FeatureExtractionService(normalizedEventRepository);
        var event = new NormalizedEventResponse(
                UUID.randomUUID(),
                EventType.SUSPICIOUS_ADMIN_ACCESS,
                SourceType.AUTH_LOG,
                Instant.parse("2026-04-23T22:00:00Z"),
                Severity.HIGH,
                "10.0.0.5",
                "10.0.0.10",
                443,
                "admin",
                "auth-service",
                "/admin/login",
                "raw",
                "normalized",
                "event:suspicious_admin_access",
                "user:admin",
                Instant.now()
        );

        when(normalizedEventRepository.findAll()).thenReturn(List.of(
                new NormalizedEvent(
                        UUID.randomUUID(),
                        EventType.FAILED_LOGIN,
                        SourceType.AUTH_LOG,
                        Instant.parse("2026-04-23T20:00:00Z"),
                        Severity.HIGH,
                        "10.0.0.5",
                        "10.0.0.10",
                        443,
                        "admin",
                        "auth-service",
                        "/admin/login",
                        "raw",
                        "normalized",
                        "event:failed_login",
                        "user:admin",
                        Instant.now()
                ),
                new NormalizedEvent(
                        UUID.randomUUID(),
                        EventType.SUSPICIOUS_ADMIN_ACCESS,
                        SourceType.AUTH_LOG,
                        Instant.parse("2026-04-23T22:00:00Z"),
                        Severity.HIGH,
                        "10.0.0.5",
                        "10.0.0.10",
                        443,
                        "admin",
                        "auth-service",
                        "/admin/login",
                        "raw",
                        "normalized",
                        "event:suspicious_admin_access",
                        "user:admin",
                        Instant.now()
                )
        ));

        var features = featureExtractionService.extract(
                event,
                List.of(new RuleEvaluationResult("ADMIN_LOGIN_FAILURE", Severity.HIGH, "title", "reason")),
                60,
                70
        );

        assertThat(features.requestCount()).isEqualTo(2);
        assertThat(features.failedLoginCount()).isEqualTo(1);
        assertThat(features.ruleMatchCount()).isEqualTo(1);
        assertThat(features.privilegedEndpointAccessCount()).isEqualTo(2);
        assertThat(features.offHoursActivity()).isEqualTo(1);
        assertThat(features.actorRiskScore()).isEqualTo(60);
    }
}
