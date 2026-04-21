package com.procurewatchbackend.dto.display;

import java.time.LocalDate;

public record GetDecisionDto(
        Long id,
        Long noticeId,
        Long contractId,
        Long institutionId,
        Long supplierId,
        String noticeNumber,
        LocalDate decisionDate,
        String subject,
        String decisionText,
        String procedureType,
        String sourceUrl
) {
}