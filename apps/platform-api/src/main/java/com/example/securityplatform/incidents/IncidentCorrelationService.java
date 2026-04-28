package com.example.securityplatform.incidents;

import com.example.securityplatform.actors.domain.Actor;
import com.example.securityplatform.common.Severity;
import com.example.securityplatform.events.domain.NormalizedEvent;
import com.example.securityplatform.events.dto.NormalizedEventResponse;
import com.example.securityplatform.events.repository.NormalizedEventRepository;
import com.example.securityplatform.incidents.domain.Incident;
import com.example.securityplatform.incidents.domain.IncidentEvent;
import com.example.securityplatform.incidents.domain.IncidentStatus;
import com.example.securityplatform.incidents.repository.IncidentEventRepository;
import com.example.securityplatform.incidents.repository.IncidentRepository;
import com.example.securityplatform.rules.service.RuleEvaluationResult;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class IncidentCorrelationService {

    private static final Duration CORRELATION_WINDOW = Duration.ofHours(6);

    private final IncidentRepository incidentRepository;
    private final IncidentEventRepository incidentEventRepository;
    private final NormalizedEventRepository normalizedEventRepository;

    public IncidentCorrelationService(
            IncidentRepository incidentRepository,
            IncidentEventRepository incidentEventRepository,
            NormalizedEventRepository normalizedEventRepository
    ) {
        this.incidentRepository = incidentRepository;
        this.incidentEventRepository = incidentEventRepository;
        this.normalizedEventRepository = normalizedEventRepository;
    }

    public IncidentCorrelationResult correlate(NormalizedEventResponse event, Actor actor, int actorRiskScore, List<RuleEvaluationResult> ruleMatches) {
        Incident incident = findMatchingIncident(event, actor)
                .orElseGet(() -> createIncident(event, actor, actorRiskScore, ruleMatches));

        if (incident.getIncidentId() == null) {
            incident = incidentRepository.save(incident);
        } else {
            incident = updateIncident(incident, event, actorRiskScore, ruleMatches);
            incident = incidentRepository.save(incident);
        }

        ensureIncidentEventLink(incident.getIncidentId(), event.eventId());

        return new IncidentCorrelationResult(incident, incident.getCreatedAt().isAfter(Instant.now().minusSeconds(5)));
    }

    private java.util.Optional<Incident> findMatchingIncident(NormalizedEventResponse event, Actor actor) {
        Instant lowerBound = event.occurredAt().minus(CORRELATION_WINDOW);

        return incidentRepository.findAll()
                .stream()
                .filter(incident -> incident.getStatus() == IncidentStatus.OPEN)
                .filter(incident -> incident.getCreatedAt() != null && !incident.getCreatedAt().isBefore(lowerBound))
                .filter(incident -> matchesActorOrCorrelation(incident, actor, event))
                .findFirst();
    }

    private boolean matchesActorOrCorrelation(Incident incident, Actor actor, NormalizedEventResponse event) {
        if (incident.getActorId() != null && incident.getActorId().equals(actor.getActorId())) {
            return true;
        }
        return incident.getCorrelationKey() != null && incident.getCorrelationKey().equals(event.correlationKey());
    }

    private Incident createIncident(NormalizedEventResponse event, Actor actor, int actorRiskScore, List<RuleEvaluationResult> ruleMatches) {
        return new Incident(
                UUID.randomUUID(),
                buildIncidentTitle(event),
                event.severity(),
                buildIncidentSummary(event, actor, ruleMatches, 1),
                event.correlationKey(),
                actor.getActorId(),
                calculateIncidentScore(event, actorRiskScore, ruleMatches, 0),
                IncidentStatus.OPEN,
                Instant.now()
        );
    }

    private Incident updateIncident(Incident incident, NormalizedEventResponse event, int actorRiskScore, List<RuleEvaluationResult> ruleMatches) {
        int existingEventCount = countIncidentEvents(incident.getIncidentId());
        incident.setSeverity(maxSeverity(incident.getSeverity(), event.severity()));
        incident.setSummary(buildIncidentSummary(event, null, ruleMatches, existingEventCount + 1));
        incident.setIncidentScore(Math.min(100, incident.getIncidentScore() + calculateIncidentScore(event, actorRiskScore, ruleMatches, existingEventCount) / 2));
        return incident;
    }

    private void ensureIncidentEventLink(UUID incidentId, UUID eventId) {
        boolean exists = incidentEventRepository.findAll()
                .stream()
                .anyMatch(link -> link.getIncidentId().equals(incidentId) && link.getEventId().equals(eventId));

        if (!exists) {
            incidentEventRepository.save(new IncidentEvent(
                    UUID.randomUUID(),
                    incidentId,
                    eventId,
                    Instant.now()
            ));
        }
    }

    private int calculateIncidentScore(
            NormalizedEventResponse event,
            int actorRiskScore,
            List<RuleEvaluationResult> ruleMatches,
            int existingEventCount
    ) {
        int severityContribution = switch (event.severity()) {
            case LOW -> 5;
            case MEDIUM -> 15;
            case HIGH -> 30;
            case CRITICAL -> 45;
        };
        int ruleContribution = ruleMatches.stream().mapToInt(match -> switch (match.severity()) {
            case LOW -> 3;
            case MEDIUM -> 8;
            case HIGH -> 15;
            case CRITICAL -> 25;
        }).sum();
        int chainContribution = Math.min(20, existingEventCount * 5);
        int multiSignalBonus = switch (event.eventType()) {
            case PORT_SCAN_ATTEMPT, FAILED_LOGIN, SUSPICIOUS_ADMIN_ACCESS -> 10;
            case BLACKLIST_IP_HIT, PRIVILEGED_ACTION -> 15;
            case ACCESS_DENIED_STORM, ERROR_SPIKE, HIGH_404_RATE -> 5;
        };

        return Math.min(100, severityContribution + ruleContribution + (actorRiskScore / 3) + chainContribution + multiSignalBonus);
    }

    private String buildIncidentTitle(NormalizedEventResponse event) {
        return switch (event.eventType()) {
            case FAILED_LOGIN -> "Authentication attack investigation";
            case PORT_SCAN_ATTEMPT -> "Network reconnaissance investigation";
            case SUSPICIOUS_ADMIN_ACCESS -> "Privileged access investigation";
            case BLACKLIST_IP_HIT -> "Blacklisted actor investigation";
            case ERROR_SPIKE -> "Critical service anomaly investigation";
            case ACCESS_DENIED_STORM -> "Denied access investigation";
            case PRIVILEGED_ACTION -> "Privileged action investigation";
            case HIGH_404_RATE -> "Web probing investigation";
        };
    }

    private String buildIncidentSummary(
            NormalizedEventResponse event,
            Actor actor,
            List<RuleEvaluationResult> ruleMatches,
            int eventCount
    ) {
        String actorText = actor == null ? "existing actor" : actor.getDisplayName();
        List<String> relatedEventTypes = findRelatedEventTypes(event);
        if (ruleMatches.isEmpty()) {
            return "Incident now contains %s correlated event(s) for %s. Latest event type is %s with no direct rule match. Related recent event types: %s."
                    .formatted(eventCount, actorText, event.eventType().name(), relatedEventTypes);
        }
        return "Incident now contains %s correlated event(s) for actor %s. Latest event type is %s and triggered rules %s. Related recent event types: %s."
                .formatted(eventCount, actorText, event.eventType().name(), ruleMatches.stream().map(RuleEvaluationResult::ruleName).toList(), relatedEventTypes);
    }

    private int countIncidentEvents(UUID incidentId) {
        return (int) incidentEventRepository.findAll()
                .stream()
                .filter(link -> link.getIncidentId().equals(incidentId))
                .count();
    }

    private List<String> findRelatedEventTypes(NormalizedEventResponse event) {
        return normalizedEventRepository.findAll()
                .stream()
                .filter(existing -> sharesCorrelation(existing, event))
                .filter(existing -> !existing.getOccurredAt().isBefore(event.occurredAt().minus(CORRELATION_WINDOW)))
                .map(NormalizedEvent::getEventType)
                .map(Enum::name)
                .distinct()
                .toList();
    }

    private boolean sharesCorrelation(NormalizedEvent existing, NormalizedEventResponse event) {
        if (event.correlationKey() != null && event.correlationKey().equals(existing.getCorrelationKey())) {
            return true;
        }
        if (event.sourceIp() != null && event.sourceIp().equals(existing.getSourceIp())) {
            return true;
        }
        return event.username() != null && event.username().equals(existing.getUsername());
    }

    private Severity maxSeverity(Severity left, Severity right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.ordinal() >= right.ordinal() ? left : right;
    }
}
