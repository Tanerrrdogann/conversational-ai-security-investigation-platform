package com.example.securityplatform.sources;

import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface SourceDefinitionRepository extends ListCrudRepository<SourceDefinition, UUID> {
}
