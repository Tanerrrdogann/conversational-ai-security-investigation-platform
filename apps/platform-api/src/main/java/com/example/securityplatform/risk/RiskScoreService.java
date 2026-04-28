package com.example.securityplatform.risk;

import com.example.securityplatform.common.Severity;
import com.example.securityplatform.events.dto.NormalizedEventResponse;
import com.example.securityplatform.risk.domain.HostRiskScore;
import com.example.securityplatform.risk.domain.UserRiskScore;
import com.example.securityplatform.risk.repository.HostRiskScoreRepository;
import com.example.securityplatform.risk.repository.UserRiskScoreRepository;
import com.example.securityplatform.rules.service.RuleEvaluationResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class RiskScoreService {

    private final HostRiskScoreRepository hostRiskScoreRepository;
    private final UserRiskScoreRepository userRiskScoreRepository;

    public RiskScoreService(HostRiskScoreRepository hostRiskScoreRepository, UserRiskScoreRepository userRiskScoreRepository) {
        this.hostRiskScoreRepository = hostRiskScoreRepository;
        this.userRiskScoreRepository = userRiskScoreRepository;
    }

    public void update(NormalizedEventResponse event, List<RuleEvaluationResult> ruleMatches, int actorRiskScore, int incidentScore) {
        if (event.destinationIp() != null && !event.destinationIp().isBlank()) {
            HostRiskScore hostRiskScore = hostRiskScoreRepository.findAll().stream()
                    .filter(existing -> event.destinationIp().equals(existing.getHostIdentifier()))
                    .findFirst()
                    .orElseGet(() -> new HostRiskScore(UUID.randomUUID(), event.destinationIp(), 0, "", Instant.now(), Instant.now()));
            hostRiskScore.setRiskScore(calculateScore(hostRiskScore.getRiskScore(), event.severity(), ruleMatches, actorRiskScore, incidentScore));
            hostRiskScore.setReason("Host targeted by %s with actor risk %s and incident score %s."
                    .formatted(event.eventType().name(), actorRiskScore, incidentScore));
            hostRiskScore.setLastSeenAt(Instant.now());
            hostRiskScoreRepository.save(hostRiskScore);
        }

        if (event.username() != null && !event.username().isBlank()) {
            UserRiskScore userRiskScore = userRiskScoreRepository.findAll().stream()
                    .filter(existing -> event.username().equals(existing.getUsername()))
                    .findFirst()
                    .orElseGet(() -> new UserRiskScore(UUID.randomUUID(), event.username(), 0, "", Instant.now(), Instant.now()));
            userRiskScore.setRiskScore(calculateScore(userRiskScore.getRiskScore(), event.severity(), ruleMatches, actorRiskScore, incidentScore));
            userRiskScore.setReason("User targeted by %s with %s matched rule(s)."
                    .formatted(event.eventType().name(), ruleMatches.size()));
            userRiskScore.setLastSeenAt(Instant.now());
            userRiskScoreRepository.save(userRiskScore);
        }
    }

    public RiskScoreSummaryResponse list() {
        List<HostRiskScoreResponse> hosts = hostRiskScoreRepository.findAll().stream()
                .sorted(Comparator.comparing(HostRiskScore::getRiskScore, Comparator.reverseOrder()))
                .map(item -> new HostRiskScoreResponse(
                        item.getHostRiskId(),
                        item.getHostIdentifier(),
                        item.getRiskScore(),
                        item.getReason(),
                        item.getLastSeenAt(),
                        item.getCreatedAt()
                ))
                .toList();
        List<UserRiskScoreResponse> users = userRiskScoreRepository.findAll().stream()
                .sorted(Comparator.comparing(UserRiskScore::getRiskScore, Comparator.reverseOrder()))
                .map(item -> new UserRiskScoreResponse(
                        item.getUserRiskId(),
                        item.getUsername(),
                        item.getRiskScore(),
                        item.getReason(),
                        item.getLastSeenAt(),
                        item.getCreatedAt()
                ))
                .toList();
        return new RiskScoreSummaryResponse(hosts, users);
    }

    private int calculateScore(int previous, Severity severity, List<RuleEvaluationResult> ruleMatches, int actorRiskScore, int incidentScore) {
        int base = switch (severity) {
            case LOW -> 5;
            case MEDIUM -> 10;
            case HIGH -> 18;
            case CRITICAL -> 30;
        };
        int ruleContribution = Math.min(25, ruleMatches.size() * 6);
        int carry = Math.min(25, previous / 2);
        int actorContribution = Math.min(20, actorRiskScore / 5);
        int incidentContribution = Math.min(20, incidentScore / 4);
        return Math.min(100, base + ruleContribution + carry + actorContribution + incidentContribution);
    }
}
