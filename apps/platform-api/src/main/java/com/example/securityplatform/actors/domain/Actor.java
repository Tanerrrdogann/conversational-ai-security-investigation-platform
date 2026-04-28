package com.example.securityplatform.actors.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("actors")
public class Actor implements Persistable<UUID> {

    @Id
    @Column("actor_id")
    private UUID actorId;

    @Column("actor_key")
    private String actorKey;

    @Column("actor_type")
    private ActorType actorType;

    @Column("display_name")
    private String displayName;

    @Column("risk_score")
    private Integer riskScore;

    @Column("last_seen_at")
    private Instant lastSeenAt;

    @Column("created_at")
    private Instant createdAt;

    @Transient
    private boolean newAggregate;

    public Actor() {
        this.newAggregate = false;
    }

    public Actor(UUID actorId, String actorKey, ActorType actorType, String displayName, Integer riskScore, Instant lastSeenAt, Instant createdAt) {
        this.actorId = actorId;
        this.actorKey = actorKey;
        this.actorType = actorType;
        this.displayName = displayName;
        this.riskScore = riskScore;
        this.lastSeenAt = lastSeenAt;
        this.createdAt = createdAt;
        this.newAggregate = true;
    }

    public UUID getActorId() {
        return actorId;
    }

    @Override
    public UUID getId() {
        return actorId;
    }

    @Override
    public boolean isNew() {
        return newAggregate;
    }

    public void markPersisted() {
        this.newAggregate = false;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public String getActorKey() {
        return actorKey;
    }

    public void setActorKey(String actorKey) {
        this.actorKey = actorKey;
    }

    public ActorType getActorType() {
        return actorType;
    }

    public void setActorType(ActorType actorType) {
        this.actorType = actorType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
