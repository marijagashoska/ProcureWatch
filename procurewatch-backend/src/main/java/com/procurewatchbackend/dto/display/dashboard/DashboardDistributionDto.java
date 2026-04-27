package com.procurewatchbackend.dto.display.dashboard;

import java.math.BigDecimal;

public record DashboardDistributionDto(
        String label,
        long count,
        BigDecimal totalContractValue
) {
}