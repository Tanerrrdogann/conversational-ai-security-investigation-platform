package com.example.securityplatform.alerts;

import com.example.securityplatform.alerts.repository.AlertRepository;
import com.example.securityplatform.investigation.AiServiceClient;
import com.example.securityplatform.investigation.AlertExplanationRequest;
import com.example.securityplatform.investigation.InvestigationResponse;
import org.springframework.stereotype.Service;

@Service
public class AlertExplanationService {

    private final AlertRepository alertRepository;
    private final AiServiceClient aiServiceClient;

    public AlertExplanationService(AlertRepository alertRepository, AiServiceClient aiServiceClient) {
        this.alertRepository = alertRepository;
        this.aiServiceClient = aiServiceClient;
    }

    public InvestigationResponse explainAlert(String alertId, String mode) {
        var alert = alertRepository.findById(java.util.UUID.fromString(alertId))
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));

        return aiServiceClient.explainAlert(new AlertExplanationRequest(
                alert.getTitle(),
                alert.getSeverity().name(),
                alert.getReason(),
                alert.getSource(),
                alert.getTriggeredRule(),
                alert.getSupportingEvidence(),
                alert.getAnomalyContribution(),
                alert.getRelatedIncidents(),
                alert.getRecommendation(),
                mode == null || mode.isBlank() ? "technical" : mode
        ));
    }
}
