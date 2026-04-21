package com.procurewatchbackend.dto.edit;

public record EditSupplierDto(
        String externalId,
        String officialName,
        String normalizedName,
        String realOwnersInfo,
        String sourceUrl
) {
}