package com.example.securityplatform.rules;

import com.example.securityplatform.common.Severity;

import java.time.Instant;
import java.util.UUID;

public record RuleManagementResponse(
        UUID ruleId,
        String name,
        String source,
        String conditionExpression,
        Integer thresholdValue,
        Severity severity,
        Integer timeWindowMinutes,
        String explanationTemplate,
        boolean enabled,
        Instant createdAt
) {
}
