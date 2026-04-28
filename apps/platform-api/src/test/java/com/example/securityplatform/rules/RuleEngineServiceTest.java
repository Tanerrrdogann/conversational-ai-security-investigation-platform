package com.example.securityplatform.rules;

import com.example.securityplatform.alerts.domain.Alert;
import com.example.securityplatform.alerts.repository.AlertRepository;
import com.example.securityplatform.common.Severity;
import com.example.securityplatform.events.domain.EventType;
import com.example.securityplatform.events.domain.SourceType;
import com.example.securityplatform.events.dto.NormalizedEventResponse;
import com.example.securityplatform.rules.RuleDefinitionRepository;
import com.example.securityplatform.rules.domain.RuleMatch;
import com.example.securityplatform.rules.repository.RuleMatchRepository;
import com.example.securityplatform.rules.service.RuleEngineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleEngineServiceTest {

    @Mock
    private RuleMatchRepository ruleMatchRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private RuleDefinitionRepository ruleDefinitionRepository;

    @InjectMocks
    private RuleEngineService ruleEngineService;

    @Test
    void evaluateGeneratesRuleMatchAndAlertForAdminLoginFailure() {
        var event = new NormalizedEventResponse(
                UUID.randomUUID(),
                EventType.FAILED_LOGIN,
                SourceType.AUTH_LOG,
                Instant.parse("2026-04-23T12:00:00Z"),
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
        );

        when(ruleDefinitionRepository.findAll()).thenReturn(List.of(
                new RuleDefinition(
                        UUID.randomUUID(),
                        "ADMIN_LOGIN_FAILURE",
                        "rule-engine",
                        "failed logins targeting admin identity",
                        20,
                        Severity.HIGH,
                        5,
                        "Admin-focused failed login activity requires investigation.",
                        true,
                        Instant.now()
                )
        ));
        when(ruleMatchRepository.save(any(RuleMatch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var results = ruleEngineService.evaluateAndGenerateAlerts(event);

        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(alertCaptor.capture());

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().ruleName()).isEqualTo("ADMIN_LOGIN_FAILURE");
        assertThat(alertCaptor.getValue().getTriggeredRule()).isEqualTo("ADMIN_LOGIN_FAILURE");
        assertThat(alertCaptor.getValue().getEventId()).isEqualTo(event.eventId());
    }
}
