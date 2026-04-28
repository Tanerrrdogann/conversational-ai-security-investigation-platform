package com.example.securityplatform.investigation.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("recommendations")
public class RecommendationRecord implements Persistable<UUID> {
    @Id
    @Column("recommendation_id")
    private UUID recommendationId;
    @Transient
    private boolean newAggregate;
    @Column("session_id")
    private UUID sessionId;
    @Column("target_type")
    private String targetType;
    @Column("target_id")
    private String targetId;
    @Column("recommendation_text")
    private String recommendationText;
    @Column("created_at")
    private Instant createdAt;

    public RecommendationRecord() {
        this.newAggregate = false;
    }

    public RecommendationRecord(UUID recommendationId, UUID sessionId, String targetType, String targetId, String recommendationText, Instant createdAt) {
        this.recommendationId = recommendationId;
        this.sessionId = sessionId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.recommendationText = recommendationText;
        this.createdAt = createdAt;
        this.newAggregate = true;
    }

    @Override
    public UUID getId() { return recommendationId; }

    @Override
    public boolean isNew() { return newAggregate; }

    public UUID getRecommendationId() { return recommendationId; }
    public UUID getSessionId() { return sessionId; }
    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public String getRecommendationText() { return recommendationText; }
    public Instant getCreatedAt() { return createdAt; }
}
