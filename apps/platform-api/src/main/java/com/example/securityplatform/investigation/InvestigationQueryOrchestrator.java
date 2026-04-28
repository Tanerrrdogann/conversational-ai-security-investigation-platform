package com.example.securityplatform.investigation;

import com.example.securityplatform.alerts.domain.Alert;
import com.example.securityplatform.alerts.repository.AlertRepository;
import com.example.securityplatform.anomaly.domain.AnomalyScore;
import com.example.securityplatform.anomaly.repository.AnomalyScoreRepository;
import com.example.securityplatform.actors.domain.Actor;
import com.example.securityplatform.actors.domain.ActorRiskHistory;
import com.example.securityplatform.actors.domain.ActorType;
import com.example.securityplatform.actors.repository.ActorRepository;
import com.example.securityplatform.actors.repository.ActorRiskHistoryRepository;
import com.example.securityplatform.events.domain.NormalizedEvent;
import com.example.securityplatform.events.repository.NormalizedEventRepository;
import com.example.securityplatform.incidents.domain.Incident;
import com.example.securityplatform.incidents.domain.IncidentEvent;
import com.example.securityplatform.incidents.repository.IncidentEventRepository;
import com.example.securityplatform.incidents.repository.IncidentRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class InvestigationQueryOrchestrator {

    private final AlertRepository alertRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentEventRepository incidentEventRepository;
    private final ActorRepository actorRepository;
    private final ActorRiskHistoryRepository actorRiskHistoryRepository;
    private final NormalizedEventRepository normalizedEventRepository;
    private final AnomalyScoreRepository anomalyScoreRepository;

    public InvestigationQueryOrchestrator(
            AlertRepository alertRepository,
            IncidentRepository incidentRepository,
            IncidentEventRepository incidentEventRepository,
            ActorRepository actorRepository,
            ActorRiskHistoryRepository actorRiskHistoryRepository,
            NormalizedEventRepository normalizedEventRepository,
            AnomalyScoreRepository anomalyScoreRepository
    ) {
        this.alertRepository = alertRepository;
        this.incidentRepository = incidentRepository;
        this.incidentEventRepository = incidentEventRepository;
        this.actorRepository = actorRepository;
        this.actorRiskHistoryRepository = actorRiskHistoryRepository;
        this.normalizedEventRepository = normalizedEventRepository;
        this.anomalyScoreRepository = anomalyScoreRepository;
    }

    public InvestigationContext buildContext(
            InvestigationIntent intent,
            String userMessage,
            InvestigationSessionState sessionState,
            String actorReference
    ) {
        return switch (intent) {
            case ALERT_EXPLANATION -> buildAlertExplanationContext(userMessage, sessionState);
            case INCIDENT_SUMMARY -> buildIncidentContext(userMessage, sessionState);
            case TOP_RISKY_ACTOR -> buildTopActorContext(userMessage, sessionState);
            case ACTOR_INVESTIGATION -> buildActorContext(actorReference, sessionState);
            case RECOMMENDATION -> buildRecommendationContext(sessionState);
            case GENERAL_SUMMARY -> buildGeneralSummaryContext();
        };
    }

    private InvestigationContext buildAlertExplanationContext(String userMessage, InvestigationSessionState sessionState) {
        Alert alert = resolveAlert(userMessage, sessionState).orElseGet(this::latestAlert);
        NormalizedEvent event = alert.getEventId() == null ? null : normalizedEventRepository.findById(alert.getEventId()).orElse(null);
        Incident incident = event == null ? null : findIncidentByEventId(event.getEventId()).orElse(null);
        AnomalyScore anomaly = event == null ? null : latestAnomalyByEventId(event.getEventId()).orElse(null);

        return new InvestigationContext(
                InvestigationIntent.ALERT_EXPLANATION,
                "alert",
                alert.getAlertId().toString(),
                alert.getTitle(),
                "Explain why alert %s matters.".formatted(alert.getTitle()),
                List.of(
                        "Alert severity: " + alert.getSeverity(),
                        "Alert source: " + alert.getSource(),
                        "Triggered rule: " + alert.getTriggeredRule(),
                        "Alert reason: " + alert.getReason(),
                        event == null ? "No normalized event was linked to this alert." : "Linked event type: " + event.getEventType(),
                        incident == null ? "No correlated incident found." : "Related incident score: " + incident.getIncidentScore(),
                        anomaly == null ? "No anomaly score found." : "Anomaly score: " + anomaly.getAnomalyScore() + " via " + anomaly.getModelName()
                ),
                List.of(
                        "Validate the evidence tied to the triggered rule.",
                        "Review adjacent events from the same actor or correlation key.",
                        "Check whether the related incident score is increasing."
                )
        );
    }

    private InvestigationContext buildIncidentContext(String userMessage, InvestigationSessionState sessionState) {
        Incident incident = resolveIncident(userMessage, sessionState).orElseGet(this::topIncident);
        List<IncidentEvent> links = incidentEventRepository.findAll().stream()
                .filter(link -> link.getIncidentId().equals(incident.getIncidentId()))
                .toList();
        List<NormalizedEvent> events = links.stream()
                .map(IncidentEvent::getEventId)
                .map(normalizedEventRepository::findById)
                .flatMap(Optional::stream)
                .toList();
        Actor actor = incident.getActorId() == null ? null : actorRepository.findById(incident.getActorId()).orElse(null);

        return new InvestigationContext(
                InvestigationIntent.INCIDENT_SUMMARY,
                "incident",
                incident.getIncidentId().toString(),
                incident.getTitle(),
                "Summarize incident %s and explain connected activity.".formatted(incident.getTitle()),
                List.of(
                        "Incident severity: " + incident.getSeverity(),
                        "Incident score: " + incident.getIncidentScore(),
                        "Incident summary: " + incident.getSummary(),
                        "Correlated event count: " + events.size(),
                        actor == null ? "No actor assigned." : "Assigned actor: " + actor.getDisplayName() + " with risk " + actor.getRiskScore(),
                        "Related event types: " + events.stream().map(event -> event.getEventType().name()).distinct().toList()
                ),
                List.of(
                        "Prioritize containment on the actor tied to this incident.",
                        "Check whether the correlated event chain is still active.",
                        "Compare the latest event type against the incident summary for drift."
                )
        );
    }

    private InvestigationContext buildTopActorContext(String userMessage, InvestigationSessionState sessionState) {
        if ("actor".equals(sessionState.lastTargetType()) && sessionState.lastTargetId() != null && !asksForIp(userMessage)) {
            return buildActorContext(null, sessionState);
        }
        Actor actor = asksForIp(userMessage) ? topActorByType(ActorType.IP) : topActor();
        return buildActorContext(actor.getDisplayName(), new InvestigationSessionState("", InvestigationIntent.TOP_RISKY_ACTOR, "actor", actor.getActorId().toString(), actor.getDisplayName(), List.of()));
    }

    private InvestigationContext buildActorContext(String actorReference, InvestigationSessionState sessionState) {
        Actor actor = resolveActor(actorReference, sessionState).orElseGet(this::topActor);
        List<ActorRiskHistory> history = actorRiskHistoryRepository.findAll().stream()
                .filter(entry -> entry.getActorId().equals(actor.getActorId()))
                .sorted(Comparator.comparing(ActorRiskHistory::getCreatedAt).reversed())
                .limit(5)
                .toList();
        List<NormalizedEvent> events = normalizedEventRepository.findAll().stream()
                .filter(event -> actorMatchesEvent(actor, event))
                .sorted(Comparator.comparing(NormalizedEvent::getOccurredAt).reversed())
                .limit(5)
                .toList();
        List<Incident> incidents = incidentRepository.findAll().stream()
                .filter(incident -> actor.getActorId().equals(incident.getActorId()))
                .sorted(Comparator.comparing(Incident::getCreatedAt).reversed())
                .limit(3)
                .toList();

        return new InvestigationContext(
                InvestigationIntent.ACTOR_INVESTIGATION,
                "actor",
                actor.getActorId().toString(),
                actor.getDisplayName(),
                "Investigate actor %s and explain why it is risky.".formatted(actor.getDisplayName()),
                List.of(
                        "Actor key: " + actor.getActorKey(),
                        "Actor type: " + actor.getActorType(),
                        "Current risk score: " + actor.getRiskScore(),
                        "Last seen at: " + actor.getLastSeenAt(),
                        "Recent event types: " + events.stream().map(event -> event.getEventType().name()).toList(),
                        "Recent incident scores: " + incidents.stream().map(Incident::getIncidentScore).toList(),
                        "Recent risk history reasons: " + history.stream().map(ActorRiskHistory::getReason).toList()
                ),
                List.of(
                        "Review the latest events tied to this actor.",
                        "Check whether the actor has crossed a response threshold.",
                        "Compare current risk against its recent history trend."
                )
        );
    }

    private InvestigationContext buildRecommendationContext(InvestigationSessionState sessionState) {
        InvestigationContext fallback = buildGeneralSummaryContext();
        String label = sessionState.lastTargetLabel() == null ? fallback.targetLabel() : sessionState.lastTargetLabel();
        return new InvestigationContext(
                InvestigationIntent.RECOMMENDATION,
                sessionState.lastTargetType() == null ? fallback.targetType() : sessionState.lastTargetType(),
                sessionState.lastTargetId() == null ? fallback.targetId() : sessionState.lastTargetId(),
                label,
                "Provide next-step recommendations for %s.".formatted(label),
                fallback.facts(),
                List.of(
                        "Validate the highest-risk evidence before taking action.",
                        "Pivot into related alerts, incidents, and actor history.",
                        "Decide whether containment, monitoring, or escalation is appropriate."
                )
        );
    }

    private InvestigationContext buildGeneralSummaryContext() {
        Actor actor = topActor();
        Incident incident = topIncident();
        Alert alert = latestAlert();
        return new InvestigationContext(
                InvestigationIntent.GENERAL_SUMMARY,
                "overview",
                "latest-overview",
                "Platform overview",
                "Summarize the current security picture for the analyst.",
                List.of(
                        "Top risky actor: " + actor.getDisplayName() + " with risk " + actor.getRiskScore(),
                        "Top incident: " + incident.getTitle() + " with score " + incident.getIncidentScore(),
                        "Latest alert: " + alert.getTitle() + " with severity " + alert.getSeverity(),
                        "Latest alert rule: " + alert.getTriggeredRule()
                ),
                List.of(
                        "Start with the top risky actor and latest incident together.",
                        "Check whether the newest alert changes the current priority order.",
                        "Use a follow-up question to drill into actor, alert, or incident detail."
                )
        );
    }

    private Optional<Alert> resolveAlert(String userMessage, InvestigationSessionState sessionState) {
        String uuid = findUuid(userMessage);
        if (uuid != null) {
            return alertRepository.findById(UUID.fromString(uuid));
        }
        if ("alert".equals(sessionState.lastTargetType()) && sessionState.lastTargetId() != null) {
            return alertRepository.findById(UUID.fromString(sessionState.lastTargetId()));
        }
        return Optional.empty();
    }

    private Optional<Incident> resolveIncident(String userMessage, InvestigationSessionState sessionState) {
        String uuid = findUuid(userMessage);
        if (uuid != null) {
            return incidentRepository.findById(UUID.fromString(uuid));
        }
        if ("incident".equals(sessionState.lastTargetType()) && sessionState.lastTargetId() != null) {
            return incidentRepository.findById(UUID.fromString(sessionState.lastTargetId()));
        }
        return Optional.empty();
    }

    private Optional<Actor> resolveActor(String actorReference, InvestigationSessionState sessionState) {
        String reference = actorReference;
        if ((reference == null || reference.isBlank()) && "actor".equals(sessionState.lastTargetType())) {
            if (sessionState.lastTargetId() != null) {
                return actorRepository.findById(UUID.fromString(sessionState.lastTargetId()));
            }
            reference = sessionState.lastTargetLabel();
        }
        String normalized = reference == null ? "" : reference.toLowerCase(Locale.ROOT);
        return actorRepository.findAll().stream()
                .filter(actor -> actor.getDisplayName().toLowerCase(Locale.ROOT).contains(normalized)
                        || actor.getActorKey().toLowerCase(Locale.ROOT).contains(normalized))
                .findFirst();
    }

    private Actor topActor() {
        return actorRepository.findAll().stream()
                .max(Comparator.comparing(Actor::getRiskScore))
                .orElseThrow(() -> new IllegalStateException("No actors available for investigation"));
    }

    private Actor topActorByType(ActorType actorType) {
        return actorRepository.findAll().stream()
                .filter(actor -> actorType.equals(actor.getActorType()))
                .max(Comparator.comparing(Actor::getRiskScore))
                .orElseGet(this::topActor);
    }

    private boolean asksForIp(String userMessage) {
        String lower = userMessage == null ? "" : userMessage.toLowerCase(Locale.ROOT);
        return lower.contains("ip");
    }

    private Incident topIncident() {
        return incidentRepository.findAll().stream()
                .max(Comparator.comparing(Incident::getIncidentScore))
                .orElseThrow(() -> new IllegalStateException("No incidents available for investigation"));
    }

    private Alert latestAlert() {
        return alertRepository.findAll().stream()
                .max(Comparator.comparing(Alert::getCreatedAt))
                .orElseThrow(() -> new IllegalStateException("No alerts available for investigation"));
    }

    private Optional<Incident> findIncidentByEventId(UUID eventId) {
        return incidentEventRepository.findAll().stream()
                .filter(link -> link.getEventId().equals(eventId))
                .map(IncidentEvent::getIncidentId)
                .map(incidentRepository::findById)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private Optional<AnomalyScore> latestAnomalyByEventId(UUID eventId) {
        return anomalyScoreRepository.findAll().stream()
                .filter(score -> score.getEventId().equals(eventId))
                .max(Comparator.comparing(AnomalyScore::getCreatedAt));
    }

    private boolean actorMatchesEvent(Actor actor, NormalizedEvent event) {
        return actor.getDisplayName().equals(event.getSourceIp())
                || actor.getDisplayName().equals(event.getUsername())
                || actor.getActorKey().equals(event.getCorrelationKey());
    }

    private String findUuid(String userMessage) {
        String[] parts = userMessage.split("\\s+");
        for (String part : parts) {
            try {
                return UUID.fromString(part.trim()).toString();
            } catch (IllegalArgumentException ignored) {
                // continue scanning tokens
            }
        }
        return null;
    }
}
