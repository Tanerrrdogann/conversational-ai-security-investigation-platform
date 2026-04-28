package com.example.securityplatform.rules;

import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface RuleDefinitionRepository extends ListCrudRepository<RuleDefinition, UUID> {
}
