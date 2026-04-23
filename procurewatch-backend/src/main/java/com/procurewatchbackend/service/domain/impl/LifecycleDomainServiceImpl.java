package com.procurewatchbackend.service.domain.impl;

import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.model.entity.Decision;
import com.procurewatchbackend.model.entity.Notice;
import com.procurewatchbackend.model.entity.PlanItem;
import com.procurewatchbackend.model.entity.ProcurementPlan;
import com.procurewatchbackend.model.entity.RealizedContract;
import com.procurewatchbackend.repository.ContractRepository;
import com.procurewatchbackend.repository.NoticeRepository;
import com.procurewatchbackend.service.domain.LifecycleDomainService;
import com.procurewatchbackend.service.domain.model.ContractLifecycleSnapshot;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LifecycleDomainServiceImpl implements LifecycleDomainService {

    private final ContractRepository contractRepository;
    private final NoticeRepository noticeRepository;

    @Override
    public ContractLifecycleSnapshot getByContractId(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Contract not found with id: " + contractId
                ));

        Notice notice = resolveNotice(contract);
        Decision decision = contract.getDecision();
        RealizedContract realizedContract = contract.getRealizedContract();
        PlanItem planItem = notice != null ? notice.getPlanItem() : null;
        ProcurementPlan plan = planItem != null ? planItem.getPlan() : null;

        boolean missingNotice = notice == null;
        boolean missingPlanItem = planItem == null;
        boolean missingPlan = plan == null;
        boolean missingDecision = decision == null;
        boolean missingRealizedContract = realizedContract == null;

        List<String> missingStages = new ArrayList<>();
        if (missingPlan) {
            missingStages.add("PLAN");
        }
        if (missingPlanItem) {
            missingStages.add("PLAN_ITEM");
        }
        if (missingNotice) {
            missingStages.add("NOTICE");
        }
        if (missingDecision) {
            missingStages.add("DECISION");
        }
        if (missingRealizedContract) {
            missingStages.add("REALIZED_CONTRACT");
        }

        return new ContractLifecycleSnapshot(
                plan,
                planItem,
                notice,
                decision,
                contract,
                realizedContract,
                missingPlan,
                missingPlanItem,
                missingNotice,
                missingDecision,
                missingRealizedContract,
                missingStages
        );
    }

    private Notice resolveNotice(Contract contract) {
        if (contract.getNoticeNumber() == null || contract.getNoticeNumber().isBlank()) {
            return null;
        }

        return noticeRepository.findFirstByNoticeNumber(contract.getNoticeNumber())
                .orElse(null);
    }
}