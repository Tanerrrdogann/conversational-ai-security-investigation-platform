package com.example.securityplatform.sources;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class SourceManagementService {

    private final SourceDefinitionRepository sourceDefinitionRepository;

    public SourceManagementService(SourceDefinitionRepository sourceDefinitionRepository) {
        this.sourceDefinitionRepository = sourceDefinitionRepository;
    }

    public List<SourceManagementResponse> list() {
        return sourceDefinitionRepository.findAll().stream()
                .sorted(Comparator.comparing(SourceDefinition::getName))
                .map(this::toResponse)
                .toList();
    }

    public SourceManagementResponse create(SourceManagementRequest request) {
        return toResponse(sourceDefinitionRepository.save(new SourceDefinition(
                UUID.randomUUID(),
                request.name(),
                request.sourceType(),
                request.ingestionMethod(),
                request.description(),
                request.enabled(),
                Instant.now()
        )));
    }

    public SourceManagementResponse update(UUID sourceId, SourceManagementRequest request) {
        SourceDefinition source = sourceDefinitionRepository.findById(sourceId)
                .orElseThrow(() -> new IllegalArgumentException("Source not found: " + sourceId));
        source.setName(request.name());
        source.setSourceType(request.sourceType());
        source.setIngestionMethod(request.ingestionMethod());
        source.setDescription(request.description());
        source.setEnabled(request.enabled());
        return toResponse(sourceDefinitionRepository.save(source));
    }

    private SourceManagementResponse toResponse(SourceDefinition source) {
        return new SourceManagementResponse(
                source.getSourceId(),
                source.getName(),
                source.getSourceType(),
                source.getIngestionMethod(),
                source.getDescription(),
                Boolean.TRUE.equals(source.getEnabled()),
                source.getCreatedAt()
        );
    }
}
