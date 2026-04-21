package com.procurewatchbackend.dto.create;

import java.time.LocalDate;

public record CreateProcurementPlanDto(
         Long institutionId,
         Integer planYear,
         LocalDate publicationDate,
         String sourceUrl
) {
}
