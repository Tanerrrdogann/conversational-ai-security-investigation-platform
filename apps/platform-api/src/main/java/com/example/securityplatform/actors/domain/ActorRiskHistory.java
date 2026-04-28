package com.example.securityplatform.actors.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("actor_risk_history")
public class ActorRiskHistory implements Persistable<UUID> {

    @Id
    @Column("history_id")
    private UUID historyId;

    @Column("actor_id")
    private UUID actorId;

    @Column("risk_score")
    private Integer riskScore;

    @Column("reason")
    private String reason;

    @Column("created_at")
    private Instant createdAt;

    @Transient
    private boolean newAggregate;

    public ActorRiskHistory() {
        this.newAggregate = false;
    }

    public ActorRiskHistory(UUID historyId, UUID actorId, Integer riskScore, String reason, Instant createdAt) {
        this.historyId = historyId;
        this.actorId = actorId;
        this.riskScore = riskScore;
        this.reason = reason;
        this.createdAt = createdAt;
        this.newAggregate = true;
    }

    public UUID getHistoryId() {
        return historyId;
    }

    @Override
    public UUID getId() {
        return historyId;
    }

    @Override
    public boolean isNew() {
        return newAggregate;
    }

    public UUID getActorId() {
        return actorId;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
