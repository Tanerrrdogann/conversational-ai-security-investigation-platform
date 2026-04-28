package com.example.securityplatform.risk.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("host_risk_scores")
public class HostRiskScore implements Persistable<UUID> {

    @Id
    @Column("host_risk_id")
    private UUID hostRiskId;

    @Column("host_identifier")
    private String hostIdentifier;

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

    public HostRiskScore() {
        this.newAggregate = false;
    }

    public HostRiskScore(UUID hostRiskId, String hostIdentifier, Integer riskScore, String reason, Instant lastSeenAt, Instant createdAt) {
        this.hostRiskId = hostRiskId;
        this.hostIdentifier = hostIdentifier;
        this.riskScore = riskScore;
        this.reason = reason;
        this.lastSeenAt = lastSeenAt;
        this.createdAt = createdAt;
        this.newAggregate = true;
    }

    public UUID getHostRiskId() {
        return hostRiskId;
    }

    @Override
    public UUID getId() {
        return hostRiskId;
    }

    @Override
    public boolean isNew() {
        return newAggregate;
    }

    public String getHostIdentifier() {
        return hostIdentifier;
    }

    public void setHostIdentifier(String hostIdentifier) {
        this.hostIdentifier = hostIdentifier;
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
