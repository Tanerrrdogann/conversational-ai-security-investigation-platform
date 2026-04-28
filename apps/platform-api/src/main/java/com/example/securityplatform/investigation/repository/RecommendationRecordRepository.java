package com.example.securityplatform.investigation.repository;

import com.example.securityplatform.investigation.domain.RecommendationRecord;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface RecommendationRecordRepository extends ListCrudRepository<RecommendationRecord, UUID> {
}
