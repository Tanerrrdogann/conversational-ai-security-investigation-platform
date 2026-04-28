package com.example.securityplatform.analysis;

import com.example.securityplatform.events.domain.RawIngestedEvent;
import com.example.securityplatform.events.domain.SourceType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class TrafficAnalysisService {

    public EventAnalysisContext analyze(RawIngestedEvent rawEvent) {
        if (rawEvent.getSourceType() != SourceType.TRAFFIC && rawEvent.getSourceType() != SourceType.SIMULATED_ATTACK) {
            return EventAnalysisContext.empty();
        }

        String message = safeLower(rawEvent.getRawMessage());
        List<String> findings = new ArrayList<>();

        boolean scanLikeTraffic = message.contains("scan")
                || message.contains("sweep")
                || message.contains("recon")
                || message.contains("probe")
                || rawEvent.getDestinationPort() != null && isSensitivePort(rawEvent.getDestinationPort());
        if (scanLikeTraffic) {
            findings.add("Traffic analysis marked this event as scan-like network behavior.");
        }

        boolean bruteForceLike = message.contains("brute")
                || message.contains("credential stuffing")
                || message.contains("password spray");
        if (bruteForceLike) {
            findings.add("Traffic analysis detected brute-force style network activity.");
        }

        boolean blacklistIndicator = message.contains("blacklist")
                || message.contains("blocked ip")
                || message.contains("threat intel");
        if (blacklistIndicator) {
            findings.add("Traffic analysis found a blacklist or threat-intel indicator.");
        }

        boolean adminTargeting = safeLower(rawEvent.getEndpoint()).contains("admin")
                || safeLower(rawEvent.getServiceName()).contains("admin");
        if (adminTargeting) {
            findings.add("Traffic analysis noticed targeting around an admin-facing path or service.");
        }

        return new EventAnalysisContext(
                List.copyOf(findings),
                scanLikeTraffic,
                bruteForceLike,
                blacklistIndicator,
                adminTargeting,
                false,
                false,
                false,
                false
        );
    }

    private boolean isSensitivePort(int port) {
        return port == 22 || port == 3389 || port == 443 || port == 3306;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
