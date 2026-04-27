package com.procurewatchbackend.service.application;

import com.procurewatchbackend.dto.display.PagedResponseDto;
import com.procurewatchbackend.dto.display.queue.HighRiskQueueItemDto;
import com.procurewatchbackend.model.enums.RiskLevel;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface HighRiskQueueApplicationService {

    PagedResponseDto<HighRiskQueueItemDto> getQueue(
            RiskLevel riskLevel,
            Long institutionId,
            Long supplierId,
            LocalDate dateFrom,
            LocalDate dateTo,
            BigDecimal minValue,
            BigDecimal maxValue,
            String flagCode,
            int page,
            int size,
            String sortBy,
            String sortDir
    );
}