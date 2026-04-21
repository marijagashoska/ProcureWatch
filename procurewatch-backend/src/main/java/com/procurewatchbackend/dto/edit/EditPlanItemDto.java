package com.procurewatchbackend.dto.edit;

public record EditPlanItemDto(
         Long planId,
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
