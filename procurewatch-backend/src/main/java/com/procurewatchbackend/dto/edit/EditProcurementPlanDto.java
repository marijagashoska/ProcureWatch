package com.procurewatchbackend.dto.edit;

import java.time.LocalDate;

public record EditProcurementPlanDto(
        Long institutionId,
        Integer planYear,
        LocalDate publicationDate,
        String sourceUrl
) {
}
