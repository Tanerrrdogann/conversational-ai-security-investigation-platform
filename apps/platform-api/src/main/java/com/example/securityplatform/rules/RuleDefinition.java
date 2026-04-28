package com.example.securityplatform.rules;

import com.example.securityplatform.common.Severity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("rules")
public class RuleDefinition {

    @Id
    @Column("rule_id")
    private UUID ruleId;
    @Column("name")
    private String name;
    @Column("source")
    private String source;
    @Column("condition_expression")
    private String conditionExpression;
    @Column("threshold_value")
    private Integer thresholdValue;
    @Column("severity")
    private Severity severity;
    @Column("time_window_minutes")
    private Integer timeWindowMinutes;
    @Column("explanation_template")
    private String explanationTemplate;
    @Column("enabled")
    private Boolean enabled;
    @Column("created_at")
    private Instant createdAt;

    public RuleDefinition() {
    }

    public RuleDefinition(UUID ruleId, String name, String source, String conditionExpression, Integer thresholdValue,
                          Severity severity, Integer timeWindowMinutes, String explanationTemplate, Boolean enabled, Instant createdAt) {
        this.ruleId = ruleId;
        this.name = name;
        this.source = source;
        this.conditionExpression = conditionExpression;
        this.thresholdValue = thresholdValue;
        this.severity = severity;
        this.timeWindowMinutes = timeWindowMinutes;
        this.explanationTemplate = explanationTemplate;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    public UUID getRuleId() { return ruleId; }
    public void setRuleId(UUID ruleId) { this.ruleId = ruleId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getConditionExpression() { return conditionExpression; }
    public void setConditionExpression(String conditionExpression) { this.conditionExpression = conditionExpression; }
    public Integer getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(Integer thresholdValue) { this.thresholdValue = thresholdValue; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public Integer getTimeWindowMinutes() { return timeWindowMinutes; }
    public void setTimeWindowMinutes(Integer timeWindowMinutes) { this.timeWindowMinutes = timeWindowMinutes; }
    public String getExplanationTemplate() { return explanationTemplate; }
    public void setExplanationTemplate(String explanationTemplate) { this.explanationTemplate = explanationTemplate; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
