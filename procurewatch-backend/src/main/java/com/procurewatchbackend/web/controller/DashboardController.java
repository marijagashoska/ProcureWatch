package com.procurewatchbackend.web.controller;

import com.procurewatchbackend.dto.display.dashboard.GetDashboardOverviewDto;
import com.procurewatchbackend.model.enums.RiskLevel;
import com.procurewatchbackend.service.application.DashboardApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardApplicationService dashboardApplicationService;

    @GetMapping("/overview")
    public ResponseEntity<GetDashboardOverviewDto> getOverview(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateTo,

            @RequestParam(required = false)
            Long institutionId,

            @RequestParam(required = false)
            Long supplierId,

            @RequestParam(required = false)
            String procedureType,

            @RequestParam(required = false)
            String contractType,

            @RequestParam(required = false)
            RiskLevel riskLevel,

            @RequestParam(defaultValue = "5")
            int limit
    ) {
        return ResponseEntity.ok(dashboardApplicationService.getOverview(
                dateFrom,
                dateTo,
                institutionId,
                supplierId,
                procedureType,
                contractType,
                riskLevel,
                limit
        ));
    }
}