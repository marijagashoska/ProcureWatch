package com.procurewatchbackend.dto.edit;

import java.time.LocalDate;

public record EditDecisionDto(
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