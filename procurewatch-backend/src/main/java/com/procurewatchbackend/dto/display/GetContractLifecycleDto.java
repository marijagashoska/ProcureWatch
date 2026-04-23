package com.procurewatchbackend.dto.display;

public record GetContractLifecycleDto(
        Long contractId,
        GetProcurementPlanDto plan,
        GetPlanItemDto planItem,
        GetNoticeDto notice,
        GetDecisionDto decision,
        GetContractDto contract,
        GetRealizedContractDto realizedContract,
        GetLifecycleMissingLinksDto missingLinks
) {
}