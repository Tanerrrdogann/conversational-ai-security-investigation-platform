package com.example.securityplatform.investigation.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("investigation_sessions")
public class InvestigationSession implements Persistable<UUID> {
    @Id
    @Column("session_id")
    private UUID sessionId;
    @Transient
    private boolean newAggregate;
    @Column("mode")
    private String mode;
    @Column("last_intent")
    private String lastIntent;
    @Column("last_target_type")
    private String lastTargetType;
    @Column("last_target_id")
    private String lastTargetId;
    @Column("last_target_label")
    private String lastTargetLabel;
    @Column("created_at")
    private Instant createdAt;
    @Column("updated_at")
    private Instant updatedAt;

    public InvestigationSession() {
        this.newAggregate = false;
    }

    public InvestigationSession(UUID sessionId, String mode, String lastIntent, String lastTargetType, String lastTargetId,
                                String lastTargetLabel, Instant createdAt, Instant updatedAt) {
        this.sessionId = sessionId;
        this.mode = mode;
        this.lastIntent = lastIntent;
        this.lastTargetType = lastTargetType;
        this.lastTargetId = lastTargetId;
        this.lastTargetLabel = lastTargetLabel;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.newAggregate = true;
    }

    @Override
    public UUID getId() { return sessionId; }

    @Override
    public boolean isNew() { return newAggregate; }

    public void markPersisted() { this.newAggregate = false; }

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getLastIntent() { return lastIntent; }
    public void setLastIntent(String lastIntent) { this.lastIntent = lastIntent; }
    public String getLastTargetType() { return lastTargetType; }
    public void setLastTargetType(String lastTargetType) { this.lastTargetType = lastTargetType; }
    public String getLastTargetId() { return lastTargetId; }
    public void setLastTargetId(String lastTargetId) { this.lastTargetId = lastTargetId; }
    public String getLastTargetLabel() { return lastTargetLabel; }
    public void setLastTargetLabel(String lastTargetLabel) { this.lastTargetLabel = lastTargetLabel; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
