package com.procurewatchbackend.dto.display;

import java.util.List;

public record GetSupplierDto(
        Long id,
        String externalId,
        String officialName,
        String normalizedName,
        String realOwnersInfo,
        String sourceUrl,
        List<Long> decisionIds
) {
}