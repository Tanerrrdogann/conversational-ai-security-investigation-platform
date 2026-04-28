package com.example.securityplatform.anomaly.service;

import com.example.securityplatform.anomaly.domain.AnomalyScore;
import com.example.securityplatform.anomaly.dto.AnomalyFeatureRequest;
import com.example.securityplatform.anomaly.dto.AnomalyScoreResponse;
import com.example.securityplatform.anomaly.repository.AnomalyScoreRepository;
import com.example.securityplatform.investigation.AiServiceClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AnomalyService {

    private final AiServiceClient aiServiceClient;
    private final AnomalyScoreRepository anomalyScoreRepository;

    public AnomalyService(AiServiceClient aiServiceClient, AnomalyScoreRepository anomalyScoreRepository) {
        this.aiServiceClient = aiServiceClient;
        this.anomalyScoreRepository = anomalyScoreRepository;
    }

    public AnomalyScoreResponse scoreAndPersist(UUID eventId, UUID actorId, UUID incidentId, AnomalyFeatureRequest features) {
        AnomalyScoreResponse response = aiServiceClient.scoreAnomaly(features);

        anomalyScoreRepository.save(new AnomalyScore(
                UUID.randomUUID(),
                eventId,
                actorId,
                incidentId,
                response.anomalyScore(),
                response.modelName(),
                response.featureSummary(),
                Instant.now()
        ));

        return response;
    }
}
