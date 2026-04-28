package com.example.securityplatform.analysis;

import com.example.securityplatform.events.domain.RawIngestedEvent;
import com.example.securityplatform.events.domain.SourceType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class LogAnalysisService {

    public EventAnalysisContext analyze(RawIngestedEvent rawEvent) {
        if (rawEvent.getSourceType() == SourceType.TRAFFIC) {
            return EventAnalysisContext.empty();
        }

        String message = safeLower(rawEvent.getRawMessage());
        String endpoint = safeLower(rawEvent.getEndpoint());
        String username = safeLower(rawEvent.getUsername());
        List<String> findings = new ArrayList<>();

        boolean bruteForceLike = message.contains("failed login")
                || message.contains("login failed")
                || message.contains("password mismatch")
                || message.contains("brute");
        if (bruteForceLike) {
            findings.add("Log analysis extracted failed-authentication behavior from the raw message.");
        }

        boolean deniedAccessPattern = message.contains("denied")
                || message.contains("forbidden")
                || message.contains("unauthorized");
        if (deniedAccessPattern) {
            findings.add("Log analysis detected denied-access language in the event.");
        }

        boolean errorSpikePattern = message.contains("error")
                || message.contains("exception")
                || message.contains("timeout");
        if (errorSpikePattern) {
            findings.add("Log analysis detected an application or service error pattern.");
        }

        boolean high404Pattern = message.contains("404")
                || message.contains("not found");
        if (high404Pattern) {
            findings.add("Log analysis detected missing-resource or 404 activity.");
        }

        boolean adminTargeting = endpoint.contains("admin")
                || username.contains("admin")
                || message.contains("privileged")
                || message.contains("sudo");
        if (adminTargeting) {
            findings.add("Log analysis noticed activity involving an admin or privileged identity.");
        }

        boolean privilegedAction = message.contains("role change")
                || message.contains("privilege escalation")
                || message.contains("policy update")
                || message.contains("created admin")
                || message.contains("sudo");
        if (privilegedAction) {
            findings.add("Log analysis marked the event as a privileged action.");
        }

        boolean blacklistIndicator = message.contains("blacklist") || message.contains("threat intel");
        if (blacklistIndicator) {
            findings.add("Log analysis found a blacklist indicator in the event.");
        }

        return new EventAnalysisContext(
                List.copyOf(findings),
                false,
                bruteForceLike,
                blacklistIndicator,
                adminTargeting,
                deniedAccessPattern,
                errorSpikePattern,
                high404Pattern,
                privilegedAction
        );
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
