package com.example.securityplatform.actors.service;

import com.example.securityplatform.actors.domain.Actor;
import com.example.securityplatform.actors.domain.ActorRiskHistory;
import com.example.securityplatform.actors.domain.ActorType;
import com.example.securityplatform.actors.repository.ActorRepository;
import com.example.securityplatform.actors.repository.ActorRiskHistoryRepository;
import com.example.securityplatform.common.Severity;
import com.example.securityplatform.events.dto.NormalizedEventResponse;
import com.example.securityplatform.rules.service.RuleEvaluationResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ActorService {

    private final ActorRepository actorRepository;
    private final ActorRiskHistoryRepository actorRiskHistoryRepository;

    public ActorService(ActorRepository actorRepository, ActorRiskHistoryRepository actorRiskHistoryRepository) {
        this.actorRepository = actorRepository;
        this.actorRiskHistoryRepository = actorRiskHistoryRepository;
    }

    public ActorResolutionResult resolveAndScoreActor(NormalizedEventResponse event, List<RuleEvaluationResult> ruleMatches) {
        String actorKey = resolveActorKey(event);
        ActorType actorType = resolveActorType(event);
        String displayName = resolveDisplayName(event, actorKey);

        Actor actor = actorRepository.findAll()
                .stream()
                .filter(existing -> actorKey.equals(existing.getActorKey()))
                .findFirst()
                .orElseGet(() -> new Actor(
                        UUID.randomUUID(),
                        actorKey,
                        actorType,
                        displayName,
                        0,
                        Instant.now(),
                        Instant.now()
                ));

        int previousRiskScore = actor.getRiskScore();
        actor.setActorType(actorType);
        actor.setDisplayName(displayName);
        actor.setLastSeenAt(Instant.now());
        actor = actorRepository.save(actor);
        actor.markPersisted();
        UUID actorId = actor.getActorId();

        List<ActorRiskHistory> historyEntries = actorRiskHistoryRepository.findAll()
                .stream()
                .filter(existing -> existing.getActorId().equals(actorId))
                .toList();

        int newRiskScore = Math.min(100, calculateRiskScore(previousRiskScore, event, ruleMatches, historyEntries));
        actor.setRiskScore(newRiskScore);
        actor = actorRepository.save(actor);

        actorRiskHistoryRepository.save(new ActorRiskHistory(
                UUID.randomUUID(),
                actor.getActorId(),
                newRiskScore,
                buildHistoryReason(event, ruleMatches, historyEntries, previousRiskScore, newRiskScore),
                Instant.now()
        ));

        return new ActorResolutionResult(actor, newRiskScore);
    }

    private String resolveActorKey(NormalizedEventResponse event) {
        if (hasText(event.username())) {
            return "user:" + event.username();
        }
        if (hasText(event.sourceIp())) {
            return "ip:" + event.sourceIp();
        }
        if (hasText(event.correlationKey())) {
            return event.correlationKey();
        }
        return "event:" + event.eventId();
    }

    private ActorType resolveActorType(NormalizedEventResponse event) {
        if (hasText(event.username())) {
            return ActorType.USER;
        }
        if (hasText(event.sourceIp())) {
            return ActorType.IP;
        }
        return ActorType.CORRELATION;
    }

    private String resolveDisplayName(NormalizedEventResponse event, String actorKey) {
        if (hasText(event.username())) {
            return event.username();
        }
        if (hasText(event.sourceIp())) {
            return event.sourceIp();
        }
        return actorKey;
    }

    private int calculateRiskScore(
            int previousRiskScore,
            NormalizedEventResponse event,
            List<RuleEvaluationResult> ruleMatches,
            List<ActorRiskHistory> historyEntries
    ) {
        int base = severityWeight(event.severity());
        int ruleContribution = ruleMatches.stream().mapToInt(match -> severityWeight(match.severity())).sum();
        int eventBonus = switch (event.eventType()) {
            case BLACKLIST_IP_HIT -> 35;
            case SUSPICIOUS_ADMIN_ACCESS -> 25;
            case PORT_SCAN_ATTEMPT, FAILED_LOGIN -> 20;
            case ACCESS_DENIED_STORM, ERROR_SPIKE -> 10;
            case PRIVILEGED_ACTION, HIGH_404_RATE -> 8;
        };
        int previousRiskCarry = Math.min(30, previousRiskScore / 2);
        long recentRepetitions = historyEntries.stream()
                .filter(entry -> !entry.getCreatedAt().isBefore(Instant.now().minusSeconds(60L * 60L * 24L)))
                .count();
        int repetitionContribution = Math.min(20, (int) recentRepetitions * 4);
        int historicalAverageContribution = historyEntries.isEmpty()
                ? 0
                : Math.min(15, (int) historyEntries.stream().mapToInt(ActorRiskHistory::getRiskScore).average().orElse(0) / 10);
        return base + ruleContribution + eventBonus + previousRiskCarry + repetitionContribution + historicalAverageContribution;
    }

    private int severityWeight(Severity severity) {
        return switch (severity) {
            case LOW -> 5;
            case MEDIUM -> 10;
            case HIGH -> 20;
            case CRITICAL -> 35;
        };
    }

    private String buildHistoryReason(
            NormalizedEventResponse event,
            List<RuleEvaluationResult> ruleMatches,
            List<ActorRiskHistory> historyEntries,
            int previousRiskScore,
            int newRiskScore
    ) {
        long recentRepetitions = historyEntries.stream()
                .filter(entry -> !entry.getCreatedAt().isBefore(Instant.now().minusSeconds(60L * 60L * 24L)))
                .count();
        if (ruleMatches.isEmpty()) {
            return "Risk moved from %s to %s for event type %s with %s recent history entries and no direct rule match."
                    .formatted(previousRiskScore, newRiskScore, event.eventType().name().toLowerCase(Locale.ROOT), recentRepetitions);
        }
        return "Risk moved from %s to %s for event type %s and triggered rules %s with %s recent history entries."
                .formatted(
                        previousRiskScore,
                        newRiskScore,
                        event.eventType().name().toLowerCase(Locale.ROOT),
                        ruleMatches.stream().map(RuleEvaluationResult::ruleName).toList(),
                        recentRepetitions
                );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
