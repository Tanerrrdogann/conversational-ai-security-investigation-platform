package com.example.securityplatform.alerts;

import com.example.securityplatform.alerts.repository.AlertRepository;
import com.example.securityplatform.common.Severity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public List<AlertResponse> list() {
        return alertRepository.findAll()
                .stream()
                .map(alert -> new AlertResponse(
                        alert.getAlertId(),
                        alert.getTitle(),
                        alert.getSeverity(),
                        alert.getReason(),
                        alert.getSource(),
                        alert.getStatus().name(),
                        alert.getEventId(),
                        alert.getTriggeredRule(),
                        alert.getSupportingEvidence(),
                        alert.getAnomalyContribution(),
                        alert.getRelatedIncidents(),
                        alert.getRecommendation(),
                        alert.getAcknowledgmentState(),
                        alert.getResolutionState(),
                        alert.getCreatedAt()
                ))
                .toList();
    }
}
