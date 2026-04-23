package com.procurewatchbackend.dto.display;

import com.procurewatchbackend.model.enums.RiskLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record GetRiskAssessmentDto(
        Long id,
        Long contractId,
        BigDecimal ruleScore,
        BigDecimal anomalyScore,
        BigDecimal similarityScore,
        BigDecimal clusterScore,
        BigDecimal finalRiskScore,
        RiskLevel riskLevel,
        Integer priorityRank,
        String modelVersion,
        LocalDateTime evaluatedAt,
        List<GetTriggeredRiskFlagDto> triggeredFlags
) {
}