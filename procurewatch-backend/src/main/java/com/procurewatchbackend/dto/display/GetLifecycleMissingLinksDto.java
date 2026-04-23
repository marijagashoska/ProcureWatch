package com.procurewatchbackend.dto.display;

import java.util.List;

public record GetLifecycleMissingLinksDto(
        boolean completeLifecycle,
        boolean missingPlan,
        boolean missingPlanItem,
        boolean missingNotice,
        boolean missingDecision,
        boolean missingRealizedContract,
        List<String> missingStages
) {
}