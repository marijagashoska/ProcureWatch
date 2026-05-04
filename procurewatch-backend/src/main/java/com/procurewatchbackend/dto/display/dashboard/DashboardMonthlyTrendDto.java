package com.procurewatchbackend.dto.display.dashboard;

import java.math.BigDecimal;

public record DashboardMonthlyTrendDto(
        Integer year,
        Integer month,
        String label,
        long contractCount,
        BigDecimal totalContractValue,
        long highRiskCount
) {
}