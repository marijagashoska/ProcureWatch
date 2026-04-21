package com.procurewatchbackend.dto.display;

import java.time.LocalDate;
import java.util.List;

public record GetProcurementPlanDto(
         Long id,
         Long institutionId,
         Integer planYear,
         LocalDate publicationDate,
         String sourceUrl,
         List<GetPlanItemDto> planItems
) {
}
