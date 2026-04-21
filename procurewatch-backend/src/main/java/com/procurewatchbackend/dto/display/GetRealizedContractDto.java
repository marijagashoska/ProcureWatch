package com.procurewatchbackend.dto.display;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GetRealizedContractDto(
        Long id,
        Long institutionId,
        Long supplierId,
        Long contractId,
        String noticeNumber,
        String subject,
        String contractType,
        String procedureType,
        BigDecimal awardedValueVat,
        BigDecimal realizedValueVat,
        BigDecimal paidValueVat,
        LocalDate publicationDate,
        LocalDate republishDate,
        String sourceUrl
) {
}