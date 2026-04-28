package com.example.securityplatform.actors.repository;

import com.example.securityplatform.actors.domain.ActorRiskHistory;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface ActorRiskHistoryRepository extends ListCrudRepository<ActorRiskHistory, UUID> {
}
