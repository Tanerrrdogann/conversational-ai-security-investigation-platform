package com.example.securityplatform.analysis;

import java.util.List;

public record EventAnalysisContext(
        List<String> findings,
        boolean scanLikeTraffic,
        boolean bruteForceLike,
        boolean blacklistIndicator,
        boolean adminTargeting,
        boolean deniedAccessPattern,
        boolean errorSpikePattern,
        boolean high404Pattern,
        boolean privilegedAction
) {
    public static EventAnalysisContext empty() {
        return new EventAnalysisContext(List.of(), false, false, false, false, false, false, false, false);
    }
}
