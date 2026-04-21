package com.procurewatchbackend.dto.create;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateContractDto(
        Long institutionId,
        Long supplierId,
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