package com.example.securityplatform.events.service;

import com.example.securityplatform.analysis.EventAnalysisOrchestrator;
import com.example.securityplatform.alerts.AlertEnrichmentService;
import com.example.securityplatform.anomaly.service.AnomalyService;
import com.example.securityplatform.anomaly.service.FeatureExtractionService;
import com.example.securityplatform.actors.service.ActorResolutionResult;
import com.example.securityplatform.actors.service.ActorService;
import com.example.securityplatform.events.domain.ProcessingStatus;
import com.example.securityplatform.events.domain.RawIngestedEvent;
import com.example.securityplatform.events.dto.IngestionResponse;
import com.example.securityplatform.events.dto.RawEventIngestionRequest;
import com.example.securityplatform.events.repository.RawIngestedEventRepository;
import com.example.securityplatform.incidents.IncidentCorrelationResult;
import com.example.securityplatform.incidents.IncidentCorrelationService;
import com.example.securityplatform.risk.RiskScoreService;
import com.example.securityplatform.rules.service.RuleEngineService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class EventIngestionService {

    private final RawIngestedEventRepository rawIngestedEventRepository;
    private final EventMirrorService eventMirrorService;
    private final EventAnalysisOrchestrator eventAnalysisOrchestrator;
    private final EventNormalizationService eventNormalizationService;
    private final RuleEngineService ruleEngineService;
    private final ActorService actorService;
    private final IncidentCorrelationService incidentCorrelationService;
    private final FeatureExtractionService featureExtractionService;
    private final AnomalyService anomalyService;
    private final AlertEnrichmentService alertEnrichmentService;
    private final RiskScoreService riskScoreService;

    public EventIngestionService(
            RawIngestedEventRepository rawIngestedEventRepository,
            EventMirrorService eventMirrorService,
            EventAnalysisOrchestrator eventAnalysisOrchestrator,
            EventNormalizationService eventNormalizationService,
            RuleEngineService ruleEngineService,
            ActorService actorService,
            IncidentCorrelationService incidentCorrelationService,
            FeatureExtractionService featureExtractionService,
            AnomalyService anomalyService,
            AlertEnrichmentService alertEnrichmentService,
            RiskScoreService riskScoreService
    ) {
        this.rawIngestedEventRepository = rawIngestedEventRepository;
        this.eventMirrorService = eventMirrorService;
        this.eventAnalysisOrchestrator = eventAnalysisOrchestrator;
        this.eventNormalizationService = eventNormalizationService;
        this.ruleEngineService = ruleEngineService;
        this.actorService = actorService;
        this.incidentCorrelationService = incidentCorrelationService;
        this.featureExtractionService = featureExtractionService;
        this.anomalyService = anomalyService;
        this.alertEnrichmentService = alertEnrichmentService;
        this.riskScoreService = riskScoreService;
    }

    public IngestionResponse ingest(RawEventIngestionRequest request) {
        var rawEvent = new RawIngestedEvent(
                UUID.randomUUID(),
                request.sourceType(),
                request.occurredAt(),
                request.sourceIp(),
                request.destinationIp(),
                request.destinationPort(),
                request.username(),
                request.serviceName(),
                request.endpoint(),
                request.rawMessage(),
                ProcessingStatus.RECEIVED,
                Instant.now()
        );

        rawEvent = rawIngestedEventRepository.save(rawEvent);
        rawEvent.markPersisted();
        eventMirrorService.mirror(rawEvent);
        var analysisContext = eventAnalysisOrchestrator.analyze(rawEvent);
        var normalizedEvent = eventNormalizationService.normalize(rawEvent, analysisContext);
        var ruleMatches = ruleEngineService.evaluateAndGenerateAlerts(normalizedEvent, analysisContext);
        ActorResolutionResult actorResult = actorService.resolveAndScoreActor(normalizedEvent, ruleMatches);
        IncidentCorrelationResult incidentResult = incidentCorrelationService.correlate(
                normalizedEvent,
                actorResult.actor(),
                actorResult.updatedRiskScore(),
                ruleMatches
        );
        var features = featureExtractionService.extract(
                normalizedEvent,
                ruleMatches,
                actorResult.updatedRiskScore(),
                incidentResult.incident().getIncidentScore()
        );
        var anomalyResponse = anomalyService.scoreAndPersist(
                normalizedEvent.eventId(),
                actorResult.actor().getActorId(),
                incidentResult.incident().getIncidentId(),
                features
        );
        riskScoreService.update(
                normalizedEvent,
                ruleMatches,
                actorResult.updatedRiskScore(),
                incidentResult.incident().getIncidentScore()
        );
        alertEnrichmentService.enrichAlertsForEvent(
                normalizedEvent.eventId(),
                normalizedEvent,
                incidentResult.incident().getIncidentId(),
                anomalyResponse.anomalyScore(),
                List.of(
                        "Validate whether the actor has related historical activity.",
                        "Review the correlated incident and linked evidence.",
                        "Confirm whether the target requires immediate containment."
                )
        );
        rawEvent.setProcessingStatus(ProcessingStatus.NORMALIZED);
        rawIngestedEventRepository.save(rawEvent);

        return new IngestionResponse(
                rawEvent.getIngestionId(),
                rawEvent.getProcessingStatus(),
                analysisContext.findings(),
                normalizedEvent,
                ruleMatches.stream().map(match -> match.ruleName()).toList(),
                actorResult.actor().getActorId(),
                actorResult.updatedRiskScore(),
                incidentResult.incident().getIncidentId(),
                incidentResult.incident().getIncidentScore(),
                anomalyResponse.anomalyScore()
        );
    }

    public List<IngestionResponse> ingestBatch(List<RawEventIngestionRequest> requests) {
        return requests.stream().map(this::ingest).toList();
    }
}
