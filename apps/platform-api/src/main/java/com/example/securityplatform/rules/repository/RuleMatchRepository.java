package com.example.securityplatform.rules.repository;

import com.example.securityplatform.rules.domain.RuleMatch;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface RuleMatchRepository extends ListCrudRepository<RuleMatch, UUID> {
}
