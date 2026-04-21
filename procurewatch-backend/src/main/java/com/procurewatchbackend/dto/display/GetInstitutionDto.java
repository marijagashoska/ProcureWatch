package com.procurewatchbackend.dto.display;

public record GetInstitutionDto(
        Long id,
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