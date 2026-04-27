package com.procurewatchbackend.service.application;

import com.procurewatchbackend.dto.display.dashboard.GetDashboardOverviewDto;
import com.procurewatchbackend.model.enums.RiskLevel;

import java.time.LocalDate;

public interface DashboardApplicationService {

    GetDashboardOverviewDto getOverview(
            LocalDate dateFrom,
            LocalDate dateTo,
            Long institutionId,
            Long supplierId,
            String procedureType,
            String contractType,
            RiskLevel riskLevel,
            int limit
    );
}