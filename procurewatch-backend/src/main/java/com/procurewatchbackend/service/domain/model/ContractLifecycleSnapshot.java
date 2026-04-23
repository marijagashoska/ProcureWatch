package com.procurewatchbackend.service.domain.model;

import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.model.entity.Decision;
import com.procurewatchbackend.model.entity.Notice;
import com.procurewatchbackend.model.entity.PlanItem;
import com.procurewatchbackend.model.entity.ProcurementPlan;
import com.procurewatchbackend.model.entity.RealizedContract;

import java.util.List;

public record ContractLifecycleSnapshot(
        ProcurementPlan plan,
        PlanItem planItem,
        Notice notice,
        Decision decision,
        Contract contract,
        RealizedContract realizedContract,
        boolean missingPlan,
        boolean missingPlanItem,
        boolean missingNotice,
        boolean missingDecision,
        boolean missingRealizedContract,
        List<String> missingStages
) {
    public boolean completeLifecycle() {
        return missingStages == null || missingStages.isEmpty();
    }
}