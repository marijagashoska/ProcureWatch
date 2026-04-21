package com.procurewatchbackend.dto.create;

import java.time.LocalDate;

public record CreateDecisionDto(
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