package com.procurewatchbackend.dto.create;

public record CreateInstitutionDto(
        String externalId,
        String officialName,
        String normalizedName,
        String institutionType,
        String city,
        String postalCode,
        String category,
        String sourceUrl
) {
}