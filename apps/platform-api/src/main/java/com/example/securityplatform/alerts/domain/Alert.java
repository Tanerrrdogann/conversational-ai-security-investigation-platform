package com.example.securityplatform.alerts.domain;

import com.example.securityplatform.common.Severity;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("alerts")
public class Alert implements Persistable<UUID> {

    @Id
    @Column("alert_id")
    private UUID alertId;

    @Column("title")
    private String title;

    @Column("severity")
    private Severity severity;

    @Column("reason")
    private String reason;

    @Column("source")
    private String source;

    @Column("status")
    private AlertStatus status;

    @Column("event_id")
    private UUID eventId;

    @Column("triggered_rule")
    private String triggeredRule;

    @Column("supporting_evidence")
    private String supportingEvidence;

    @Column("anomaly_contribution")
    private Double anomalyContribution;

    @Column("related_incidents")
    private String relatedIncidents;

    @Column("recommendation")
    private String recommendation;

    @Column("acknowledgment_state")
    private String acknowledgmentState;

    @Column("resolution_state")
    private String resolutionState;

    @Column("created_at")
    private Instant createdAt;

    @Transient
    private boolean newAggregate;

    public Alert() {
        this.newAggregate = false;
    }

    public Alert(
            UUID alertId,
            String title,
            Severity severity,
            String reason,
            String source,
            AlertStatus status,
            UUID eventId,
            String triggeredRule,
            String supportingEvidence,
            Double anomalyContribution,
            String relatedIncidents,
            String recommendation,
            String acknowledgmentState,
            String resolutionState,
            Instant createdAt
    ) {
        this.alertId = alertId;
        this.title = title;
        this.severity = severity;
        this.reason = reason;
        this.source = source;
        this.status = status;
        this.eventId = eventId;
        this.triggeredRule = triggeredRule;
        this.supportingEvidence = supportingEvidence;
        this.anomalyContribution = anomalyContribution;
        this.relatedIncidents = relatedIncidents;
        this.recommendation = recommendation;
        this.acknowledgmentState = acknowledgmentState;
        this.resolutionState = resolutionState;
        this.createdAt = createdAt;
        this.newAggregate = true;
    }

    public UUID getAlertId() {
        return alertId;
    }

    @Override
    public UUID getId() {
        return alertId;
    }

    @Override
    public boolean isNew() {
        return newAggregate;
    }

    public void setAlertId(UUID alertId) {
        this.alertId = alertId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getTriggeredRule() {
        return triggeredRule;
    }

    public void setTriggeredRule(String triggeredRule) {
        this.triggeredRule = triggeredRule;
    }

    public String getSupportingEvidence() {
        return supportingEvidence;
    }

    public void setSupportingEvidence(String supportingEvidence) {
        this.supportingEvidence = supportingEvidence;
    }

    public Double getAnomalyContribution() {
        return anomalyContribution;
    }

    public void setAnomalyContribution(Double anomalyContribution) {
        this.anomalyContribution = anomalyContribution;
    }

    public String getRelatedIncidents() {
        return relatedIncidents;
    }

    public void setRelatedIncidents(String relatedIncidents) {
        this.relatedIncidents = relatedIncidents;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getAcknowledgmentState() {
        return acknowledgmentState;
    }

    public void setAcknowledgmentState(String acknowledgmentState) {
        this.acknowledgmentState = acknowledgmentState;
    }

    public String getResolutionState() {
        return resolutionState;
    }

    public void setResolutionState(String resolutionState) {
        this.resolutionState = resolutionState;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
