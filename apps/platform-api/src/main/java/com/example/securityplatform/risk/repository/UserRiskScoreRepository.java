package com.example.securityplatform.risk.repository;

import com.example.securityplatform.risk.domain.UserRiskScore;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface UserRiskScoreRepository extends ListCrudRepository<UserRiskScore, UUID> {
}
