package com.procurewatchbackend.web.controller;

import com.procurewatchbackend.dto.display.PagedResponseDto;
import com.procurewatchbackend.dto.display.queue.HighRiskQueueItemDto;
import com.procurewatchbackend.model.enums.RiskLevel;
import com.procurewatchbackend.service.application.HighRiskQueueApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/high-risk-queue")
@RequiredArgsConstructor
public class HighRiskQueueController {

    private final HighRiskQueueApplicationService highRiskQueueApplicationService;

    @GetMapping
    public ResponseEntity<PagedResponseDto<HighRiskQueueItemDto>> getQueue(
            @RequestParam(required = false) RiskLevel riskLevel,
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Long supplierId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateTo,

            @RequestParam(required = false) BigDecimal minValue,
            @RequestParam(required = false) BigDecimal maxValue,
            @RequestParam(required = false) String flagCode,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "priorityRank") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok(
                highRiskQueueApplicationService.getQueue(
                        riskLevel,
                        institutionId,
                        supplierId,
                        dateFrom,
                        dateTo,
                        minValue,
                        maxValue,
                        flagCode,
                        page,
                        size,
                        sortBy,
                        sortDir
                )
        );
    }
}