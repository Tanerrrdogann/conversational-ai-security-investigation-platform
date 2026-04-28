package com.example.securityplatform.investigation.repository;

import com.example.securityplatform.investigation.domain.InvestigationSession;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface InvestigationSessionRepository extends ListCrudRepository<InvestigationSession, UUID> {
}
