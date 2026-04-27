package com.procurewatchbackend.dto.display.dashboard;

import java.math.BigDecimal;

public record DashboardTopInstitutionDto(
        Long institutionId,
        String institutionName,
        long contractCount,
        BigDecimal totalContractValue,
        long highRiskCount
) {
}