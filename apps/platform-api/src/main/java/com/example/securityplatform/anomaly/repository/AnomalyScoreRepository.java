package com.example.securityplatform.anomaly.repository;

import com.example.securityplatform.anomaly.domain.AnomalyScore;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface AnomalyScoreRepository extends ListCrudRepository<AnomalyScore, UUID> {
}
