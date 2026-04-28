package com.example.securityplatform.investigation.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("chat_messages")
public class ChatMessage implements Persistable<UUID> {
    @Id
    @Column("message_id")
    private UUID messageId;
    @Transient
    private boolean newAggregate;
    @Column("session_id")
    private UUID sessionId;
    @Column("role")
    private String role;
    @Column("message_text")
    private String messageText;
    @Column("created_at")
    private Instant createdAt;

    public ChatMessage() {
        this.newAggregate = false;
    }

    public ChatMessage(UUID messageId, UUID sessionId, String role, String messageText, Instant createdAt) {
        this.messageId = messageId;
        this.sessionId = sessionId;
        this.role = role;
        this.messageText = messageText;
        this.createdAt = createdAt;
        this.newAggregate = true;
    }

    @Override
    public UUID getId() { return messageId; }

    @Override
    public boolean isNew() { return newAggregate; }

    public UUID getMessageId() { return messageId; }
    public UUID getSessionId() { return sessionId; }
    public String getRole() { return role; }
    public String getMessageText() { return messageText; }
    public Instant getCreatedAt() { return createdAt; }
}
