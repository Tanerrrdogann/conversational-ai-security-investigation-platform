package com.example.securityplatform.anomaly.service;

import com.example.securityplatform.anomaly.dto.AnomalyFeatureRequest;
import com.example.securityplatform.events.domain.NormalizedEvent;
import com.example.securityplatform.events.dto.NormalizedEventResponse;
import com.example.securityplatform.events.repository.NormalizedEventRepository;
import com.example.securityplatform.rules.service.RuleEvaluationResult;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

@Service
public class FeatureExtractionService {

    private static final Duration FEATURE_WINDOW = Duration.ofHours(6);

    private final NormalizedEventRepository normalizedEventRepository;

    public FeatureExtractionService(NormalizedEventRepository normalizedEventRepository) {
        this.normalizedEventRepository = normalizedEventRepository;
    }

    public AnomalyFeatureRequest extract(
            NormalizedEventResponse event,
            List<RuleEvaluationResult> ruleMatches,
            int actorRiskScore,
            int incidentScore
    ) {
        List<NormalizedEvent> relatedEvents = normalizedEventRepository.findAll()
                .stream()
                .filter(existing -> isWithinWindow(existing, event))
                .filter(existing -> sharesEntity(existing, event))
                .toList();

        return new AnomalyFeatureRequest(
                Math.max(1, relatedEvents.size()),
                Math.max(0, countUniquePorts(relatedEvents)),
                countEventType(relatedEvents, "FAILED_LOGIN"),
                Math.max(0, countUniqueEndpoints(relatedEvents)),
                countEventType(relatedEvents, "ACCESS_DENIED_STORM"),
                countPrivilegedEndpointAccess(relatedEvents),
                relatedEvents.stream().anyMatch(this::isOffHours) ? 1 : 0,
                ruleMatches.size(),
                actorRiskScore,
                incidentScore
        );
    }

    private boolean isWithinWindow(NormalizedEvent existing, NormalizedEventResponse current) {
        return !existing.getOccurredAt().isBefore(current.occurredAt().minus(FEATURE_WINDOW))
                && !existing.getOccurredAt().isAfter(current.occurredAt());
    }

    private boolean sharesEntity(NormalizedEvent existing, NormalizedEventResponse current) {
        if (hasText(current.correlationKey()) && current.correlationKey().equals(existing.getCorrelationKey())) {
            return true;
        }
        if (hasText(current.sourceIp()) && current.sourceIp().equals(existing.getSourceIp())) {
            return true;
        }
        return hasText(current.username()) && current.username().equals(existing.getUsername());
    }

    private int countUniquePorts(List<NormalizedEvent> events) {
        return (int) events.stream()
                .map(NormalizedEvent::getDestinationPort)
                .filter(port -> port != null)
                .distinct()
                .count();
    }

    private int countEventType(List<NormalizedEvent> events, String eventType) {
        return (int) events.stream()
                .filter(event -> event.getEventType().name().equals(eventType))
                .count();
    }

    private int countUniqueEndpoints(List<NormalizedEvent> events) {
        return (int) events.stream()
                .map(NormalizedEvent::getEndpoint)
                .filter(this::hasText)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .distinct()
                .count();
    }

    private int countPrivilegedEndpointAccess(List<NormalizedEvent> events) {
        return (int) events.stream().filter(this::isPrivilegedEndpoint).count();
    }

    private boolean isOffHours(NormalizedEventResponse event) {
        int hour = event.occurredAt().atZone(ZoneOffset.UTC).getHour();
        return hour < 8 || hour >= 20;
    }

    private boolean isOffHours(NormalizedEvent event) {
        int hour = event.getOccurredAt().atZone(ZoneOffset.UTC).getHour();
        return hour < 8 || hour >= 20;
    }

    private boolean isPrivilegedEndpoint(NormalizedEventResponse event) {
        String endpoint = event.endpoint() == null ? "" : event.endpoint().toLowerCase(Locale.ROOT);
        return endpoint.contains("admin") || endpoint.contains("privileged") || event.eventType().name().equals("SUSPICIOUS_ADMIN_ACCESS");
    }

    private boolean isPrivilegedEndpoint(NormalizedEvent event) {
        String endpoint = event.getEndpoint() == null ? "" : event.getEndpoint().toLowerCase(Locale.ROOT);
        return endpoint.contains("admin")
                || endpoint.contains("privileged")
                || event.getEventType().name().equals("SUSPICIOUS_ADMIN_ACCESS")
                || event.getEventType().name().equals("PRIVILEGED_ACTION");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
