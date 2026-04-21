package com.procurewatchbackend.dto.display;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GetContractDto(
        Long id,
        Long institutionId,
        Long supplierId,
        Long decisionId,
        Long realizedContractId,
        String noticeNumber,
        String subject,
        String contractType,
        String procedureType,
        LocalDate contractDate,
        LocalDate publicationDate,
        BigDecimal estimatedValueVat,
        BigDecimal contractValueVat,
        String currency,
        String sourceUrl
) {
}