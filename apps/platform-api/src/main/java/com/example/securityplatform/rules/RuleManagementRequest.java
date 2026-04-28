package com.example.securityplatform.rules;

import com.example.securityplatform.common.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RuleManagementRequest(
        @NotBlank String name,
        @NotBlank String source,
        @NotBlank String conditionExpression,
        Integer thresholdValue,
        @NotNull Severity severity,
        Integer timeWindowMinutes,
        @NotBlank String explanationTemplate,
        boolean enabled
) {
}
