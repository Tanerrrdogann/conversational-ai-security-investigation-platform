package com.example.securityplatform.events;

import com.example.securityplatform.analysis.EventAnalysisContext;
import com.example.securityplatform.analysis.EventAnalysisOrchestrator;
import com.example.securityplatform.anomaly.dto.AnomalyScoreResponse;
import com.example.securityplatform.anomaly.service.AnomalyService;
import com.example.securityplatform.anomaly.service.FeatureExtractionService;
import com.example.securityplatform.actors.domain.Actor;
import com.example.securityplatform.actors.domain.ActorType;
import com.example.securityplatform.actors.service.ActorResolutionResult;
import com.example.securityplatform.actors.service.ActorService;
import com.example.securityplatform.common.Severity;
import com.example.securityplatform.events.domain.EventType;
import com.example.securityplatform.events.domain.NormalizedEvent;
import com.example.securityplatform.events.domain.ProcessingStatus;
import com.example.securityplatform.events.domain.RawIngestedEvent;
import com.example.securityplatform.events.domain.SourceType;
import com.example.securityplatform.events.dto.IngestionResponse;
import com.example.securityplatform.events.dto.NormalizedEventResponse;
import com.example.securityplatform.events.dto.RawEventIngestionRequest;
import com.example.securityplatform.events.repository.RawIngestedEventRepository;
import com.example.securityplatform.events.repository.NormalizedEventRepository;
import com.example.securityplatform.events.service.EventIngestionService;
import com.example.securityplatform.events.service.EventMirrorService;
import com.example.securityplatform.events.service.EventNormalizationService;
import com.example.securityplatform.incidents.IncidentCorrelationResult;
import com.example.securityplatform.incidents.IncidentCorrelationService;
import com.example.securityplatform.incidents.domain.Incident;
import com.example.securityplatform.incidents.domain.IncidentStatus;
import com.example.securityplatform.alerts.AlertEnrichmentService;
import com.example.securityplatform.risk.RiskScoreService;
import com.example.securityplatform.rules.service.RuleEngineService;
import com.example.securityplatform.rules.service.RuleEvaluationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventIngestionServiceTest {

    @Mock
    private RawIngestedEventRepository rawIngestedEventRepository;

    @Mock
    private NormalizedEventRepository normalizedEventRepository;

    @Mock
    private EventMirrorService eventMirrorService;

    @Mock
    private EventAnalysisOrchestrator eventAnalysisOrchestrator;

    @Mock
    private EventNormalizationService eventNormalizationService;

    @Mock
    private RuleEngineService ruleEngineService;

    @Mock
    private ActorService actorService;

    @Mock
    private IncidentCorrelationService incidentCorrelationService;

    @Mock
    private FeatureExtractionService featureExtractionService;

    @Mock
    private AnomalyService anomalyService;

    @Mock
    private AlertEnrichmentService alertEnrichmentService;

    @Mock
    private RiskScoreService riskScoreService;

    @InjectMocks
    private EventIngestionService eventIngestionService;

    @Test
    void ingestStoresRawEventAndReturnsNormalizedOutput() {
        var request = new RawEventIngestionRequest(
                SourceType.TRAFFIC,
                Instant.parse("2026-04-23T13:00:00Z"),
                "185.24.91.18",
                "10.0.0.10",
                22,
                null,
                "gateway",
                "/admin",
                "port scan detected from suspicious source"
        );

        when(rawIngestedEventRepository.save(any(RawIngestedEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(eventAnalysisOrchestrator.analyze(any(RawIngestedEvent.class)))
                .thenReturn(new EventAnalysisContext(
                        List.of(
                                "Traffic analysis marked this event as scan-like network behavior.",
                                "Traffic analysis noticed targeting around an admin-facing path or service."
                        ),
                        true,
                        false,
                        false,
                        true,
                        false,
                        false,
                        false,
                        false
                ));
        when(eventNormalizationService.normalize(any(RawIngestedEvent.class), any(EventAnalysisContext.class)))
                .thenReturn(new NormalizedEventResponse(
                        UUID.randomUUID(),
                        EventType.PORT_SCAN_ATTEMPT,
                        SourceType.TRAFFIC,
                        request.occurredAt(),
                        Severity.HIGH,
                        request.sourceIp(),
                        request.destinationIp(),
                        request.destinationPort(),
                        request.username(),
                        request.serviceName(),
                        request.endpoint(),
                        request.rawMessage(),
                        "Detected scanning-like traffic targeting port 22",
                        "event:port_scan_attempt",
                        "ip:185.24.91.18",
                        Instant.now()
                ));
        when(ruleEngineService.evaluateAndGenerateAlerts(any(NormalizedEventResponse.class), any(EventAnalysisContext.class)))
                .thenReturn(List.of(new RuleEvaluationResult(
                        "SENSITIVE_PORT_SCAN",
                        Severity.HIGH,
                        "Sensitive port scan detected",
                        "reason"
                )));
        when(actorService.resolveAndScoreActor(any(NormalizedEventResponse.class), any()))
                .thenReturn(new ActorResolutionResult(
                        new Actor(
                                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                                "ip:185.24.91.18",
                                ActorType.IP,
                                "185.24.91.18",
                                55,
                                Instant.now(),
                                Instant.now()
                        ),
                        55
                ));
        when(incidentCorrelationService.correlate(any(), any(), any(Integer.class), any()))
                .thenReturn(new IncidentCorrelationResult(
                        new Incident(
                                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                                "Network reconnaissance investigation",
                                Severity.HIGH,
                                "Incident groups event type PORT_SCAN_ATTEMPT for actor 185.24.91.18 after rules [SENSITIVE_PORT_SCAN] triggered.",
                                "ip:185.24.91.18",
                                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                                48,
                                IncidentStatus.OPEN,
                                Instant.now()
                        ),
                        true
                ));
        when(featureExtractionService.extract(any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(new com.example.securityplatform.anomaly.dto.AnomalyFeatureRequest(1, 1, 0, 1, 0, 1, 1, 1, 55, 48));
        when(anomalyService.scoreAndPersist(any(), any(), any(), any()))
                .thenReturn(new AnomalyScoreResponse(0.82, "isolation-forest-baseline", "High actor risk + sensitive port + off-hours"));

        IngestionResponse response = eventIngestionService.ingest(request);

        assertThat(response.ingestionId()).isNotNull();
        assertThat(response.processingStatus()).isEqualTo(ProcessingStatus.NORMALIZED);
        assertThat(response.analysisFindings()).hasSize(2);
        assertThat(response.normalizedEvent().eventType()).isEqualTo(EventType.PORT_SCAN_ATTEMPT);
        assertThat(response.triggeredRules()).containsExactly("SENSITIVE_PORT_SCAN");
        assertThat(response.actorRiskScore()).isEqualTo(55);
        assertThat(response.incidentScore()).isEqualTo(48);
        assertThat(response.anomalyScore()).isEqualTo(0.82);
    }
}
