package com.example.securityplatform.rules.service;

import com.example.securityplatform.common.Severity;

public record RuleEvaluationResult(
        String ruleName,
        Severity severity,
        String title,
        String reason
) {
}
