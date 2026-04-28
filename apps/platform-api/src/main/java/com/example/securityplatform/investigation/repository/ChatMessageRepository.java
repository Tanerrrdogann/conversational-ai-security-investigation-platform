package com.example.securityplatform.investigation.repository;

import com.example.securityplatform.investigation.domain.ChatMessage;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface ChatMessageRepository extends ListCrudRepository<ChatMessage, UUID> {
}
