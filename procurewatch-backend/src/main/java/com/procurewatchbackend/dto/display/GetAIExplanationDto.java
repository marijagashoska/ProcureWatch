package com.procurewatchbackend.dto.display;

import java.time.LocalDateTime;

public record GetAIExplanationDto(
        Long id,
        Long contractId,
        Long riskAssessmentId,
        String summaryText,
        String explanationText,
        String recommendationText,
        String generatorType,
        String modelVersion,
        LocalDateTime generatedAt
) {
}