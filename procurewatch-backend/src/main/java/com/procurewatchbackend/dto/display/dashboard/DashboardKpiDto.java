package com.procurewatchbackend.dto.display.dashboard;

import java.math.BigDecimal;

public record DashboardKpiDto(
        long totalContracts,
        BigDecimal totalContractValue,
        long totalNotices,
        long totalPlanItems,
        long highRiskCount
) {
}