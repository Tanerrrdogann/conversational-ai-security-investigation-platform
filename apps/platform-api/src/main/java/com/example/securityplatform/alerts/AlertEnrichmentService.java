package com.example.securityplatform.alerts;

import com.example.securityplatform.alerts.repository.AlertRepository;
import com.example.securityplatform.events.dto.NormalizedEventResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AlertEnrichmentService {

    private final AlertRepository alertRepository;

    public AlertEnrichmentService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public void enrichAlertsForEvent(
            UUID eventId,
            NormalizedEventResponse normalizedEvent,
            UUID incidentId,
            Double anomalyScore,
            List<String> recommendations
    ) {
        String joinedRecommendations = recommendations == null || recommendations.isEmpty()
                ? "Review correlated evidence and validate actor intent."
                : String.join(" ", recommendations);
        alertRepository.findAll().stream()
                .filter(alert -> eventId.equals(alert.getEventId()))
                .forEach(alert -> {
                    alert.setSupportingEvidence(normalizedEvent.normalizedMessage());
                    alert.setAnomalyContribution(anomalyScore);
                    alert.setRelatedIncidents(incidentId == null ? null : incidentId.toString());
                    alert.setRecommendation(joinedRecommendations);
                    alert.setAcknowledgmentState("PENDING");
                    alert.setResolutionState("UNRESOLVED");
                    alertRepository.save(alert);
                });
    }
}
