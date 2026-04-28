package com.example.securityplatform.risk.repository;

import com.example.securityplatform.risk.domain.HostRiskScore;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface HostRiskScoreRepository extends ListCrudRepository<HostRiskScore, UUID> {
}
