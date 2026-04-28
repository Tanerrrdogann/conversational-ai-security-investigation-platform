package com.example.securityplatform.rules.service;

import com.example.securityplatform.analysis.EventAnalysisContext;
import com.example.securityplatform.alerts.domain.Alert;
import com.example.securityplatform.alerts.domain.AlertStatus;
import com.example.securityplatform.alerts.repository.AlertRepository;
import com.example.securityplatform.common.Severity;
import com.example.securityplatform.events.domain.EventType;
import com.example.securityplatform.events.dto.NormalizedEventResponse;
import com.example.securityplatform.rules.RuleDefinition;
import com.example.securityplatform.rules.RuleDefinitionRepository;
import com.example.securityplatform.rules.domain.RuleMatch;
import com.example.securityplatform.rules.repository.RuleMatchRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RuleEngineService {

    private final RuleMatchRepository ruleMatchRepository;
    private final AlertRepository alertRepository;
    private final RuleDefinitionRepository ruleDefinitionRepository;

    public RuleEngineService(RuleMatchRepository ruleMatchRepository, AlertRepository alertRepository, RuleDefinitionRepository ruleDefinitionRepository) {
        this.ruleMatchRepository = ruleMatchRepository;
        this.alertRepository = alertRepository;
        this.ruleDefinitionRepository = ruleDefinitionRepository;
    }

    public List<RuleEvaluationResult> evaluateAndGenerateAlerts(NormalizedEventResponse event) {
        return evaluateAndGenerateAlerts(event, EventAnalysisContext.empty());
    }

    public List<RuleEvaluationResult> evaluateAndGenerateAlerts(
            NormalizedEventResponse event,
            EventAnalysisContext analysisContext
    ) {
        List<RuleEvaluationResult> matches = evaluate(event, analysisContext);

        for (RuleEvaluationResult match : matches) {
            ruleMatchRepository.save(new RuleMatch(
                    UUID.randomUUID(),
                    match.ruleName(),
                    event.eventId(),
                    match.severity(),
                    match.reason(),
                    Instant.now()
            ));

            alertRepository.save(new Alert(
                    UUID.randomUUID(),
                    match.title(),
                    match.severity(),
                    match.reason(),
                    "rule-engine",
                    AlertStatus.OPEN,
                    event.eventId(),
                    match.ruleName(),
                    event.normalizedMessage(),
                    null,
                    null,
                    "Review alert evidence, actor context, and related incidents.",
                    "PENDING",
                    "UNRESOLVED",
                    Instant.now()
            ));
        }

        return matches;
    }

    private List<RuleEvaluationResult> evaluate(NormalizedEventResponse event, EventAnalysisContext analysisContext) {
        List<RuleEvaluationResult> matches = new ArrayList<>();
        Map<String, RuleDefinition> enabledRules = ruleDefinitionRepository.findAll().stream()
                .filter(rule -> Boolean.TRUE.equals(rule.getEnabled()))
                .collect(Collectors.toMap(RuleDefinition::getName, rule -> rule, (left, right) -> left));

        if ((event.eventType() == EventType.BLACKLIST_IP_HIT || analysisContext.blacklistIndicator()) && enabledRules.containsKey("BLACKLIST_IP_ACCESS")) {
            matches.add(buildResult(enabledRules.get("BLACKLIST_IP_ACCESS"), "Blacklisted actor detected",
                    "The event matched the blacklist access rule because source IP %s was identified as blacklisted."
                            .formatted(defaultValue(event.sourceIp(), "unknown-source"))));
        }

        if ((event.eventType() == EventType.SUSPICIOUS_ADMIN_ACCESS || analysisContext.adminTargeting()) && enabledRules.containsKey("SUSPICIOUS_ADMIN_ENDPOINT_ACCESS")) {
            matches.add(buildResult(enabledRules.get("SUSPICIOUS_ADMIN_ENDPOINT_ACCESS"), "Suspicious admin access detected",
                    "The event matched the admin access rule because a privileged or admin-oriented action targeted %s."
                            .formatted(defaultValue(event.endpoint(), "an unknown endpoint"))));
        }

        if ((event.eventType() == EventType.PORT_SCAN_ATTEMPT || analysisContext.scanLikeTraffic()) && isSensitivePort(event.destinationPort()) && enabledRules.containsKey("SENSITIVE_PORT_SCAN")) {
            matches.add(buildResult(enabledRules.get("SENSITIVE_PORT_SCAN"), "Sensitive port scan detected",
                    "The event matched the sensitive port scan rule because source IP %s targeted port %s."
                            .formatted(defaultValue(event.sourceIp(), "unknown-source"), defaultValue(event.destinationPort(), "unknown-port"))));
        }

        if ((event.eventType() == EventType.FAILED_LOGIN || analysisContext.bruteForceLike()) && targetsAdminIdentity(event) && enabledRules.containsKey("ADMIN_LOGIN_FAILURE")) {
            matches.add(buildResult(enabledRules.get("ADMIN_LOGIN_FAILURE"), "Admin-focused failed login detected",
                    "The event matched the admin login failure rule because the failed login targeted username %s on endpoint %s."
                            .formatted(defaultValue(event.username(), "unknown-user"), defaultValue(event.endpoint(), "unknown-endpoint"))));
        }

        if ((event.eventType() == EventType.ERROR_SPIKE || analysisContext.errorSpikePattern()) && isCriticalService(event.serviceName()) && enabledRules.containsKey("CRITICAL_SERVICE_ERROR_SPIKE")) {
            matches.add(buildResult(enabledRules.get("CRITICAL_SERVICE_ERROR_SPIKE"), "Critical service error spike detected",
                    "The event matched the critical service error rule because service %s appears business-critical and emitted an error spike."
                            .formatted(defaultValue(event.serviceName(), "unknown-service"))));
        }

        if ((event.eventType() == EventType.ACCESS_DENIED_STORM || analysisContext.deniedAccessPattern()) && enabledRules.containsKey("REPEATED_ACCESS_DENIED")) {
            matches.add(buildResult(enabledRules.get("REPEATED_ACCESS_DENIED"), "Repeated access denied activity detected",
                    "The event matched the denied access rule because access to %s was repeatedly rejected for actor %s."
                            .formatted(defaultValue(event.endpoint(), "an unknown endpoint"), defaultValue(event.sourceIp(), "unknown-source"))));
        }

        if ((event.eventType() == EventType.HIGH_404_RATE || analysisContext.high404Pattern()) && enabledRules.containsKey("HIGH_404_RESOURCE_PROBING")) {
            matches.add(buildResult(enabledRules.get("HIGH_404_RESOURCE_PROBING"), "High 404 probing activity detected",
                    "The event matched the 404 probing rule because actor %s generated missing-resource behavior against %s."
                            .formatted(defaultValue(event.sourceIp(), "unknown-source"), defaultValue(event.endpoint(), "an unknown path"))));
        }

        if ((event.eventType() == EventType.PRIVILEGED_ACTION || analysisContext.privilegedAction()) && enabledRules.containsKey("PRIVILEGED_ACTION_REVIEW")) {
            matches.add(buildResult(enabledRules.get("PRIVILEGED_ACTION_REVIEW"), "Privileged action requires investigation",
                    "The event matched the privileged action review rule because user %s performed a high-impact action on %s."
                            .formatted(defaultValue(event.username(), "unknown-user"), defaultValue(event.serviceName(), "an unknown service"))));
        }

        return matches;
    }

    private RuleEvaluationResult buildResult(RuleDefinition definition, String title, String fallbackReason) {
        return new RuleEvaluationResult(
                definition.getName(),
                definition.getSeverity(),
                title,
                definition.getExplanationTemplate() == null || definition.getExplanationTemplate().isBlank()
                        ? fallbackReason
                        : definition.getExplanationTemplate()
        );
    }

    private boolean isSensitivePort(Integer port) {
        return port != null && (port == 22 || port == 3389 || port == 443 || port == 3306);
    }

    private boolean targetsAdminIdentity(NormalizedEventResponse event) {
        String username = safeLower(event.username());
        String endpoint = safeLower(event.endpoint());
        return username.contains("admin") || endpoint.contains("admin");
    }

    private boolean isCriticalService(String serviceName) {
        String value = safeLower(serviceName);
        return value.contains("auth") || value.contains("payment") || value.contains("gateway") || value.contains("identity");
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String defaultValue(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }
}
