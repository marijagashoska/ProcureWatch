package com.procurewatchbackend.dto.display;

public record GetPlanItemDto(
         Long id,
         String subject,
         String cpvCode,
         String contractType,
         String procedureType,
         String expectedStartMonth,
         Boolean hasNotice,
         String notes,
         String sourceUrl
) {
}
