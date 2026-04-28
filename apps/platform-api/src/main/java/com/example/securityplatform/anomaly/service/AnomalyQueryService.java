package com.example.securityplatform.anomaly.service;

import com.example.securityplatform.anomaly.AnomalyRecordResponse;
import com.example.securityplatform.anomaly.repository.AnomalyScoreRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class AnomalyQueryService {

    private final AnomalyScoreRepository anomalyScoreRepository;

    public AnomalyQueryService(AnomalyScoreRepository anomalyScoreRepository) {
        this.anomalyScoreRepository = anomalyScoreRepository;
    }

    public List<AnomalyRecordResponse> list() {
        return anomalyScoreRepository.findAll().stream()
                .sorted(Comparator.comparing(score -> score.getAnomalyScore() == null ? 0.0 : score.getAnomalyScore(), Comparator.reverseOrder()))
                .map(score -> new AnomalyRecordResponse(
                        score.getAnomalyId(),
                        score.getEventId(),
                        score.getActorId(),
                        score.getIncidentId(),
                        score.getAnomalyScore(),
                        score.getModelName(),
                        score.getFeatureSummary(),
                        score.getCreatedAt()
                ))
                .toList();
    }
}
