package com.example.securityplatform.investigation;

import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class InvestigationIntentService {

    private static final Pattern IP_PATTERN = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");

    public InvestigationIntent detectIntent(String message, InvestigationSessionState sessionState) {
        String lower = message.toLowerCase(Locale.ROOT);

        if (isFollowUp(lower) && sessionState.lastIntent() != null) {
            return sessionState.lastIntent();
        }
        if (lower.contains("recommend") || lower.contains("what should") || lower.contains("what do i check") || lower.contains("next step")) {
            return InvestigationIntent.RECOMMENDATION;
        }
        if (lower.contains("riskiest") || lower.contains("most risky") || lower.contains("risky ip") || lower.contains("risky actor") || lower.contains("top actor")) {
            return InvestigationIntent.TOP_RISKY_ACTOR;
        }
        if (lower.contains("incident") || lower.contains("chain") || lower.contains("connected")) {
            return InvestigationIntent.INCIDENT_SUMMARY;
        }
        if (lower.contains("alert") || lower.contains("alarm") || lower.contains("why did") || lower.contains("trigger")) {
            return InvestigationIntent.ALERT_EXPLANATION;
        }
        if (IP_PATTERN.matcher(lower).find() || lower.contains("ip ") || lower.contains("user") || lower.contains("actor")) {
            return InvestigationIntent.ACTOR_INVESTIGATION;
        }
        return InvestigationIntent.GENERAL_SUMMARY;
    }

    public String extractActorReference(String message, InvestigationSessionState sessionState) {
        var matcher = IP_PATTERN.matcher(message);
        if (matcher.find()) {
            return matcher.group();
        }
        String lower = message.toLowerCase(Locale.ROOT);
        if (lower.contains("this ip") || lower.contains("this actor") || lower.contains("same actor")) {
            return sessionState.lastTargetLabel();
        }
        return null;
    }

    private boolean isFollowUp(String lower) {
        String normalized = lower.replaceAll("[?!.,;:]+", "").trim();
        return normalized.equals("why")
                || normalized.equals("why is that")
                || lower.contains("is it connected")
                || lower.contains("previous activity")
                || lower.contains("continue")
                || lower.contains("make it shorter")
                || lower.contains("technical version")
                || lower.contains("plain version");
    }
}
