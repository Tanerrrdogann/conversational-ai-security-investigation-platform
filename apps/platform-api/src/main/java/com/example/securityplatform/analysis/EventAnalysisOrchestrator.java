package com.example.securityplatform.analysis;

import com.example.securityplatform.events.domain.RawIngestedEvent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventAnalysisOrchestrator {

    private final TrafficAnalysisService trafficAnalysisService;
    private final LogAnalysisService logAnalysisService;

    public EventAnalysisOrchestrator(
            TrafficAnalysisService trafficAnalysisService,
            LogAnalysisService logAnalysisService
    ) {
        this.trafficAnalysisService = trafficAnalysisService;
        this.logAnalysisService = logAnalysisService;
    }

    public EventAnalysisContext analyze(RawIngestedEvent rawEvent) {
        EventAnalysisContext trafficContext = trafficAnalysisService.analyze(rawEvent);
        EventAnalysisContext logContext = logAnalysisService.analyze(rawEvent);

        List<String> findings = new ArrayList<>();
        findings.addAll(trafficContext.findings());
        findings.addAll(logContext.findings());

        return new EventAnalysisContext(
                List.copyOf(findings),
                trafficContext.scanLikeTraffic() || logContext.scanLikeTraffic(),
                trafficContext.bruteForceLike() || logContext.bruteForceLike(),
                trafficContext.blacklistIndicator() || logContext.blacklistIndicator(),
                trafficContext.adminTargeting() || logContext.adminTargeting(),
                trafficContext.deniedAccessPattern() || logContext.deniedAccessPattern(),
                trafficContext.errorSpikePattern() || logContext.errorSpikePattern(),
                trafficContext.high404Pattern() || logContext.high404Pattern(),
                trafficContext.privilegedAction() || logContext.privilegedAction()
        );
    }
}
