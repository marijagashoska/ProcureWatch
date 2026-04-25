package com.procurewatchbackend.ai;

import java.util.List;

public record AiRiskResponse(
        Long contractId,
        double anomalyScore,
        String riskLevel,
        String summaryText,
        String explanationText,
        String recommendationText,
        List<String> reasons
) {}