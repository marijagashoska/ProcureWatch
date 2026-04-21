package com.procurewatchbackend.dto.create;

public record CreateSupplierDto(
        String externalId,
        String officialName,
        String normalizedName,
        String realOwnersInfo,
        String sourceUrl
) {
}