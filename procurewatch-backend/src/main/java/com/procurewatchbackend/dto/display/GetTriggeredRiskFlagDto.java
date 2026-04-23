package com.procurewatchbackend.dto.display;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GetTriggeredRiskFlagDto(
        Long id,
        String flagCode,
        String flagName,
        String flagDescription,
        BigDecimal weight,
        String measuredValue,
        String thresholdValue,
        LocalDateTime createdAt
) {
}