package com.procurewatchbackend.dto.ai;

public record GeneratedExplanationDto(
        String summaryText,
        String explanationText,
        String recommendationText
) {
}