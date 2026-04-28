package com.example.securityplatform.risk.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("user_risk_scores")
public class UserRiskScore implements Persistable<UUID> {

    @Id
    @Column("user_risk_id")
    private UUID userRiskId;

    @Column("username")
    private String username;

    @Column("risk_score")
    private Integer riskScore;

    @Column("reason")
    private String reason;

    @Column("last_seen_at")
    private Instant lastSeenAt;

    @Column("created_at")
    private Instant createdAt;

    @Transient
    private boolean newAggregate;

    public UserRiskScore() {
        this.newAggregate = false;
    }

    public UserRiskScore(UUID userRiskId, String username, Integer riskScore, String reason, Instant lastSeenAt, Instant createdAt) {
        this.userRiskId = userRiskId;
        this.username = username;
        this.riskScore = riskScore;
        this.reason = reason;
        this.lastSeenAt = lastSeenAt;
        this.createdAt = createdAt;
        this.newAggregate = true;
    }

    public UUID getUserRiskId() {
        return userRiskId;
    }

    @Override
    public UUID getId() {
        return userRiskId;
    }

    @Override
    public boolean isNew() {
        return newAggregate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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
}
