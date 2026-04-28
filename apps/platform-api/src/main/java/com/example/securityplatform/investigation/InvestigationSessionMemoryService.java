package com.example.securityplatform.investigation;

import com.example.securityplatform.investigation.domain.ChatMessage;
import com.example.securityplatform.investigation.domain.InvestigationSession;
import com.example.securityplatform.investigation.domain.RecommendationRecord;
import com.example.securityplatform.investigation.repository.ChatMessageRepository;
import com.example.securityplatform.investigation.repository.InvestigationSessionRepository;
import com.example.securityplatform.investigation.repository.RecommendationRecordRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Comparator;

@Service
public class InvestigationSessionMemoryService {

    private static final int MAX_TURNS = 8;

    private final InvestigationSessionRepository investigationSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RecommendationRecordRepository recommendationRecordRepository;
    private final StringRedisTemplate stringRedisTemplate;

    public InvestigationSessionMemoryService(
            InvestigationSessionRepository investigationSessionRepository,
            ChatMessageRepository chatMessageRepository,
            RecommendationRecordRepository recommendationRecordRepository,
            StringRedisTemplate stringRedisTemplate
    ) {
        this.investigationSessionRepository = investigationSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.recommendationRecordRepository = recommendationRecordRepository;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public InvestigationSessionState getOrCreate(String requestedSessionId) {
        UUID sessionUuid = requestedSessionId == null || requestedSessionId.isBlank()
                ? UUID.randomUUID()
                : UUID.fromString(requestedSessionId);

        InvestigationSession session = investigationSessionRepository.findById(sessionUuid)
                .orElseGet(() -> investigationSessionRepository.save(new InvestigationSession(
                        sessionUuid,
                        "technical",
                        null,
                        null,
                        null,
                        null,
                        Instant.now(),
                        Instant.now()
                )));

        List<InvestigationTurn> turns = chatMessageRepository.findAll().stream()
                .filter(message -> message.getSessionId().equals(sessionUuid))
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .map(message -> new InvestigationTurn(message.getRole(), message.getMessageText()))
                .toList();

        int fromIndex = Math.max(0, turns.size() - MAX_TURNS);
        return new InvestigationSessionState(
                sessionUuid.toString(),
                session.getLastIntent() == null ? null : InvestigationIntent.valueOf(session.getLastIntent()),
                session.getLastTargetType(),
                session.getLastTargetId(),
                session.getLastTargetLabel(),
                turns.subList(fromIndex, turns.size())
        );
    }

    public void remember(
            String sessionId,
            InvestigationIntent intent,
            String targetType,
            String targetId,
            String targetLabel,
            String userMessage,
            String assistantMessage,
            String mode,
            List<String> recommendations
    ) {
        UUID sessionUuid = UUID.fromString(sessionId);
        InvestigationSession session = investigationSessionRepository.findById(sessionUuid)
                .orElseGet(() -> new InvestigationSession(sessionUuid, mode, null, null, null, null, Instant.now(), Instant.now()));
        session.setMode(mode);
        session.setLastIntent(intent.name());
        session.setLastTargetType(targetType);
        session.setLastTargetId(targetId);
        session.setLastTargetLabel(targetLabel);
        session.setUpdatedAt(Instant.now());
        investigationSessionRepository.save(session);

        chatMessageRepository.save(new ChatMessage(UUID.randomUUID(), sessionUuid, "user", userMessage, Instant.now()));
        chatMessageRepository.save(new ChatMessage(UUID.randomUUID(), sessionUuid, "assistant", assistantMessage, Instant.now()));

        for (String recommendation : recommendations) {
            recommendationRecordRepository.save(new RecommendationRecord(
                    UUID.randomUUID(),
                    sessionUuid,
                    targetType,
                    targetId,
                    recommendation,
                    Instant.now()
            ));
        }

        stringRedisTemplate.opsForValue().set(
                "investigation-session:" + sessionId,
                String.join("|", targetType == null ? "" : targetType, targetId == null ? "" : targetId, targetLabel == null ? "" : targetLabel)
        );
    }
}
