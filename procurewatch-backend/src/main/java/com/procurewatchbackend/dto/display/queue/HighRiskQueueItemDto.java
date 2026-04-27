package com.procurewatchbackend.dto.display.queue;

import com.procurewatchbackend.model.enums.RiskLevel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record HighRiskQueueItemDto(
        Long contractId,
        String noticeNumber,
        String subject,

        Long institutionId,
        String institutionName,

        Long supplierId,
        String supplierName,

        BigDecimal contractValueVat,
        LocalDate contractDate,

        BigDecimal finalRiskScore,
        RiskLevel riskLevel,
        Integer priorityRank,

        int flagCount,
        List<String> flagNames,
        String explanationPreview
) {
}