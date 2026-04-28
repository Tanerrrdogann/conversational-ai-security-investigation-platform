package com.example.securityplatform.incidents;

import com.example.securityplatform.actors.domain.Actor;
import com.example.securityplatform.actors.domain.ActorType;
import com.example.securityplatform.common.Severity;
import com.example.securityplatform.events.domain.EventType;
import com.example.securityplatform.events.domain.NormalizedEvent;
import com.example.securityplatform.events.domain.SourceType;
import com.example.securityplatform.events.dto.NormalizedEventResponse;
import com.example.securityplatform.events.repository.NormalizedEventRepository;
import com.example.securityplatform.incidents.domain.Incident;
import com.example.securityplatform.incidents.domain.IncidentEvent;
import com.example.securityplatform.incidents.domain.IncidentStatus;
import com.example.securityplatform.incidents.repository.IncidentEventRepository;
import com.example.securityplatform.incidents.repository.IncidentRepository;
import com.example.securityplatform.rules.service.RuleEvaluationResult;
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
class IncidentCorrelationServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private IncidentEventRepository incidentEventRepository;

    @Mock
    private NormalizedEventRepository normalizedEventRepository;

    @InjectMocks
    private IncidentCorrelationService incidentCorrelationService;

    @Test
    void correlateCreatesIncidentAndIncidentEventLink() {
        var event = new NormalizedEventResponse(
                UUID.randomUUID(),
                EventType.PORT_SCAN_ATTEMPT,
                SourceType.TRAFFIC,
                Instant.parse("2026-04-23T12:00:00Z"),
                Severity.HIGH,
                "185.24.91.18",
                "10.0.0.10",
                22,
                null,
                "gateway",
                "/admin",
                "raw",
                "normalized",
                "event:port_scan_attempt",
                "ip:185.24.91.18",
                Instant.now()
        );

        var actor = new Actor(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                "ip:185.24.91.18",
                ActorType.IP,
                "185.24.91.18",
                55,
                Instant.now(),
                Instant.now()
        );

        when(incidentRepository.findAll()).thenReturn(List.of());
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(incidentEventRepository.findAll()).thenReturn(List.of());
        when(incidentEventRepository.save(any(IncidentEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(normalizedEventRepository.findAll()).thenReturn(List.of(
                new NormalizedEvent(
                        UUID.randomUUID(),
                        EventType.FAILED_LOGIN,
                        SourceType.AUTH_LOG,
                        Instant.parse("2026-04-23T11:45:00Z"),
                        Severity.HIGH,
                        "185.24.91.18",
                        "10.0.0.10",
                        443,
                        "admin",
                        "auth-service",
                        "/admin/login",
                        "raw",
                        "normalized",
                        "event:failed_login",
                        "ip:185.24.91.18",
                        Instant.now()
                )
        ));

        var result = incidentCorrelationService.correlate(
                event,
                actor,
                55,
                List.of(new RuleEvaluationResult("SENSITIVE_PORT_SCAN", Severity.HIGH, "title", "reason"))
        );

        ArgumentCaptor<Incident> incidentCaptor = ArgumentCaptor.forClass(Incident.class);
        verify(incidentRepository).save(incidentCaptor.capture());

        assertThat(result.incident().getIncidentId()).isNotNull();
        assertThat(incidentCaptor.getValue().getActorId()).isEqualTo(actor.getActorId());
        assertThat(incidentCaptor.getValue().getIncidentScore()).isGreaterThan(0);
        assertThat(incidentCaptor.getValue().getSummary()).contains("correlated event");
    }
}
