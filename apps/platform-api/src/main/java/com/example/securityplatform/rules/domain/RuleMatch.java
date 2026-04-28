package com.example.securityplatform.rules.domain;

import com.example.securityplatform.common.Severity;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("rule_matches")
public class RuleMatch implements Persistable<UUID> {

    @Id
    @Column("match_id")
    private UUID matchId;

    @Column("rule_name")
    private String ruleName;

    @Column("event_id")
    private UUID eventId;

    @Column("severity")
    private Severity severity;

    @Column("reason")
    private String reason;

    @Column("created_at")
    private Instant createdAt;

    @Transient
    private boolean newAggregate;

    public RuleMatch() {
        this.newAggregate = false;
    }

    public RuleMatch(UUID matchId, String ruleName, UUID eventId, Severity severity, String reason, Instant createdAt) {
        this.matchId = matchId;
        this.ruleName = ruleName;
        this.eventId = eventId;
        this.severity = severity;
        this.reason = reason;
        this.createdAt = createdAt;
        this.newAggregate = true;
    }

    public UUID getMatchId() {
        return matchId;
    }

    @Override
    public UUID getId() {
        return matchId;
    }

    @Override
    public boolean isNew() {
        return newAggregate;
    }

    public String getRuleName() {
        return ruleName;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
