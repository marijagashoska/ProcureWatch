package com.procurewatchbackend.dto.display.dashboard;

import java.math.BigDecimal;

public record DashboardTopSupplierDto(
        Long supplierId,
        String supplierName,
        long contractCount,
        BigDecimal totalContractValue,
        long highRiskCount
) {
}