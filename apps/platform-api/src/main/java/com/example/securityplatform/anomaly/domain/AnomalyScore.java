package com.example.securityplatform.anomaly.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("anomaly_scores")
public class AnomalyScore implements Persistable<UUID> {

    @Id
    @Column("anomaly_id")
    private UUID anomalyId;

    @Column("event_id")
    private UUID eventId;

    @Column("actor_id")
    private UUID actorId;

    @Column("incident_id")
    private UUID incidentId;

    @Column("anomaly_score")
    private Double anomalyScore;

    @Column("model_name")
    private String modelName;

    @Column("feature_summary")
    private String featureSummary;

    @Column("created_at")
    private Instant createdAt;

    @Transient
    private boolean newAggregate;

    public AnomalyScore() {
        this.newAggregate = false;
    }

    public AnomalyScore(UUID anomalyId, UUID eventId, UUID actorId, UUID incidentId, Double anomalyScore, String modelName, String featureSummary, Instant createdAt) {
        this.anomalyId = anomalyId;
        this.eventId = eventId;
        this.actorId = actorId;
        this.incidentId = incidentId;
        this.anomalyScore = anomalyScore;
        this.modelName = modelName;
        this.featureSummary = featureSummary;
        this.createdAt = createdAt;
        this.newAggregate = true;
    }

    public UUID getAnomalyId() {
        return anomalyId;
    }

    @Override
    public UUID getId() {
        return anomalyId;
    }

    @Override
    public boolean isNew() {
        return newAggregate;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getActorId() {
        return actorId;
    }

    public UUID getIncidentId() {
        return incidentId;
    }

    public Double getAnomalyScore() {
        return anomalyScore;
    }

    public String getModelName() {
        return modelName;
    }

    public String getFeatureSummary() {
        return featureSummary;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
