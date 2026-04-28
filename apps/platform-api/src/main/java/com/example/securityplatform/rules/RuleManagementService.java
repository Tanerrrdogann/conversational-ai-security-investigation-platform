package com.example.securityplatform.rules;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class RuleManagementService {

    private final RuleDefinitionRepository ruleDefinitionRepository;

    public RuleManagementService(RuleDefinitionRepository ruleDefinitionRepository) {
        this.ruleDefinitionRepository = ruleDefinitionRepository;
    }

    public List<RuleManagementResponse> list() {
        return ruleDefinitionRepository.findAll().stream()
                .sorted(Comparator.comparing(RuleDefinition::getName))
                .map(this::toResponse)
                .toList();
    }

    public RuleManagementResponse create(RuleManagementRequest request) {
        RuleDefinition definition = new RuleDefinition(
                UUID.randomUUID(),
                request.name(),
                request.source(),
                request.conditionExpression(),
                request.thresholdValue(),
                request.severity(),
                request.timeWindowMinutes(),
                request.explanationTemplate(),
                request.enabled(),
                Instant.now()
        );
        return toResponse(ruleDefinitionRepository.save(definition));
    }

    public RuleManagementResponse update(UUID ruleId, RuleManagementRequest request) {
        RuleDefinition definition = ruleDefinitionRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));
        definition.setName(request.name());
        definition.setSource(request.source());
        definition.setConditionExpression(request.conditionExpression());
        definition.setThresholdValue(request.thresholdValue());
        definition.setSeverity(request.severity());
        definition.setTimeWindowMinutes(request.timeWindowMinutes());
        definition.setExplanationTemplate(request.explanationTemplate());
        definition.setEnabled(request.enabled());
        return toResponse(ruleDefinitionRepository.save(definition));
    }

    private RuleManagementResponse toResponse(RuleDefinition definition) {
        return new RuleManagementResponse(
                definition.getRuleId(),
                definition.getName(),
                definition.getSource(),
                definition.getConditionExpression(),
                definition.getThresholdValue(),
                definition.getSeverity(),
                definition.getTimeWindowMinutes(),
                definition.getExplanationTemplate(),
                Boolean.TRUE.equals(definition.getEnabled()),
                definition.getCreatedAt()
        );
    }
}
