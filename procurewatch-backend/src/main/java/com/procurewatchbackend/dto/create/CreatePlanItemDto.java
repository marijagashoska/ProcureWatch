package com.procurewatchbackend.dto.create;

public record CreatePlanItemDto(
         Long planId,
         String subject,
         String cpvCode,
         String contractType,
         String procedureType,
         String expectedStartMonth,
         Boolean hasNotice,
         String notes,
         String sourceUrl) {
}
