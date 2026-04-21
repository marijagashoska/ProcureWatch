package com.procurewatchbackend.dto.edit;

public record EditInstitutionDto(
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