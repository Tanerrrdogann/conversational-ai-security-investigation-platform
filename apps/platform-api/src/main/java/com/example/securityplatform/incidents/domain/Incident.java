package com.example.securityplatform.incidents.domain;

import com.example.securityplatform.common.Severity;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("incidents")
public class Incident implements Persistable<UUID> {

    @Id
    @Column("incident_id")
    private UUID incidentId;

    @Column("title")
    private String title;

    @Column("severity")
    private Severity severity;

    @Column("summary")
    private String summary;

    @Column("correlation_key")
    private String correlationKey;

    @Column("actor_id")
    private UUID actorId;

    @Column("incident_score")
    private Integer incidentScore;

    @Column("status")
    private IncidentStatus status;

    @Column("created_at")
    private Instant createdAt;

    @Transient
    private boolean newAggregate;

    public Incident() {
        this.newAggregate = false;
    }

    public Incident(UUID incidentId, String title, Severity severity, String summary, String correlationKey, UUID actorId, Integer incidentScore, IncidentStatus status, Instant createdAt) {
        this.incidentId = incidentId;
        this.title = title;
        this.severity = severity;
        this.summary = summary;
        this.correlationKey = correlationKey;
        this.actorId = actorId;
        this.incidentScore = incidentScore;
        this.status = status;
        this.createdAt = createdAt;
        this.newAggregate = true;
    }

    public UUID getIncidentId() {
        return incidentId;
    }

    @Override
    public UUID getId() {
        return incidentId;
    }

    @Override
    public boolean isNew() {
        return newAggregate;
    }

    public void setIncidentId(UUID incidentId) {
        this.incidentId = incidentId;
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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCorrelationKey() {
        return correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public Integer getIncidentScore() {
        return incidentScore;
    }

    public void setIncidentScore(Integer incidentScore) {
        this.incidentScore = incidentScore;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
