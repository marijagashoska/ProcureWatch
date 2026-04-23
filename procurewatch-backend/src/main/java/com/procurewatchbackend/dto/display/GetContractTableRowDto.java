package com.procurewatchbackend.dto.display;

import com.procurewatchbackend.model.enums.RiskLevel;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GetContractTableRowDto(
        Long contractId,
        String noticeNumber,
        String institutionName,
        String supplierName,
        String subject,
        String contractType,
        String procedureType,
        BigDecimal contractValueVat,
        LocalDate contractDate,
        BigDecimal riskScore,
        RiskLevel riskLevel
) {
}