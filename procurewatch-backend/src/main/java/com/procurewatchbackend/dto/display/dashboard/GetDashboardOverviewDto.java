package com.procurewatchbackend.dto.display.dashboard;

import java.util.List;

public record GetDashboardOverviewDto(
        DashboardKpiDto kpis,
        List<DashboardTopInstitutionDto> topInstitutions,
        List<DashboardTopSupplierDto> topSuppliers,
        List<DashboardDistributionDto> procedureTypeDistribution,
        List<DashboardDistributionDto> contractTypeDistribution,
        List<DashboardMonthlyTrendDto> monthlyTrends
) {
}