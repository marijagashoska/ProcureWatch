package com.procurewatchbackend.service.application.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.procurewatchbackend.dto.display.GetContractDto;
import com.procurewatchbackend.dto.display.GetContractLifecycleDto;
import com.procurewatchbackend.dto.display.GetDecisionDto;
import com.procurewatchbackend.dto.display.GetLifecycleMissingLinksDto;
import com.procurewatchbackend.dto.display.GetNoticeDto;
import com.procurewatchbackend.dto.display.GetPlanItemDto;
import com.procurewatchbackend.dto.display.GetProcurementPlanDto;
import com.procurewatchbackend.dto.display.GetRealizedContractDto;
import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.model.entity.Decision;
import com.procurewatchbackend.model.entity.Notice;
import com.procurewatchbackend.model.entity.PlanItem;
import com.procurewatchbackend.model.entity.ProcurementPlan;
import com.procurewatchbackend.model.entity.RealizedContract;
import com.procurewatchbackend.service.application.LifecycleApplicationService;
import com.procurewatchbackend.service.domain.LifecycleDomainService;
import com.procurewatchbackend.service.domain.model.ContractLifecycleSnapshot;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LifecycleApplicationServiceImpl implements LifecycleApplicationService {

    private final LifecycleDomainService lifecycleDomainService;

    @Override
    public GetContractLifecycleDto getByContractId(Long contractId) {
        ContractLifecycleSnapshot snapshot = lifecycleDomainService.getByContractId(contractId);

        return new GetContractLifecycleDto(
                snapshot.contract().getId(),
                mapPlan(snapshot.plan()),
                mapPlanItem(snapshot.planItem()),
                mapNotice(snapshot.notice()),
                mapDecision(snapshot.decision()),
                mapContract(snapshot.contract()),
                mapRealizedContract(snapshot.realizedContract()),
                new GetLifecycleMissingLinksDto(
                        snapshot.completeLifecycle(),
                        snapshot.missingPlan(),
                        snapshot.missingPlanItem(),
                        snapshot.missingNotice(),
                        snapshot.missingDecision(),
                        snapshot.missingRealizedContract(),
                        snapshot.missingStages()
                )
        );
    }

    private GetProcurementPlanDto mapPlan(ProcurementPlan plan) {
        if (plan == null) {
            return null;
        }

        List<GetPlanItemDto> planItems = plan.getPlanItems() == null
                ? Collections.emptyList()
                : plan.getPlanItems().stream()
                .map(this::mapPlanItem)
                .toList();

        return new GetProcurementPlanDto(
                plan.getId(),
                plan.getInstitution().getId(),
                plan.getPlanYear(),
                plan.getPublicationDate(),
                plan.getSourceUrl(),
                planItems
        );
    }

    private GetPlanItemDto mapPlanItem(PlanItem item) {
        if (item == null) {
            return null;
        }

        return new GetPlanItemDto(
                item.getId(),
                item.getSubject(),
                item.getCpvCode(),
                item.getContractType(),
                item.getProcedureType(),
                item.getExpectedStartMonth(),
                item.getHasNotice(),
                item.getNotes(),
                item.getSourceUrl()
        );
    }

    private GetNoticeDto mapNotice(Notice notice) {
        if (notice == null) {
            return null;
        }

        List<Long> decisionIds = notice.getDecisions() == null
                ? Collections.emptyList()
                : notice.getDecisions().stream().map(Decision::getId).toList();

        return new GetNoticeDto(
                notice.getId(),
                notice.getInstitution().getId(),
                notice.getPlanItem() != null ? notice.getPlanItem().getId() : null,
                decisionIds,
                notice.getNoticeNumber(),
                notice.getSubject(),
                notice.getContractType(),
                notice.getProcedureType(),
                notice.getPublicationDate(),
                notice.getDeadlineDate(),
                notice.getSourceUrl()
        );
    }

    private GetDecisionDto mapDecision(Decision decision) {
        if (decision == null) {
            return null;
        }

        return new GetDecisionDto(
                decision.getId(),
                decision.getNotice() != null ? decision.getNotice().getId() : null,
                decision.getContract() != null ? decision.getContract().getId() : null,
                decision.getInstitution() != null ? decision.getInstitution().getId() : null,
                decision.getSupplier() != null ? decision.getSupplier().getId() : null,
                decision.getNoticeNumber(),
                decision.getDecisionDate(),
                decision.getSubject(),
                decision.getDecisionText(),
                decision.getProcedureType(),
                decision.getSourceUrl()
        );
    }

    private GetContractDto mapContract(Contract contract) {
        return new GetContractDto(
                contract.getId(),
                contract.getInstitution().getId(),
                contract.getSupplier() != null ? contract.getSupplier().getId() : null,
                contract.getDecision() != null ? contract.getDecision().getId() : null,
                contract.getRealizedContract() != null ? contract.getRealizedContract().getId() : null,
                contract.getNoticeNumber(),
                contract.getSubject(),
                contract.getContractType(),
                contract.getProcedureType(),
                contract.getContractDate(),
                contract.getPublicationDate(),
                contract.getEstimatedValueVat(),
                contract.getContractValueVat(),
                contract.getCurrency(),
                contract.getSourceUrl()
        );
    }

    private GetRealizedContractDto mapRealizedContract(RealizedContract realizedContract) {
        if (realizedContract == null) {
            return null;
        }

        return new GetRealizedContractDto(
                realizedContract.getId(),
                realizedContract.getInstitution() != null ? realizedContract.getInstitution().getId() : null,
                realizedContract.getSupplier() != null ? realizedContract.getSupplier().getId() : null,
                realizedContract.getContract() != null ? realizedContract.getContract().getId() : null,
                realizedContract.getNoticeNumber(),
                realizedContract.getSubject(),
                realizedContract.getContractType(),
                realizedContract.getProcedureType(),
                realizedContract.getAwardedValueVat(),
                realizedContract.getRealizedValueVat(),
                realizedContract.getPaidValueVat(),
                realizedContract.getPublicationDate(),
                realizedContract.getRepublishDate(),
                realizedContract.getSourceUrl()
        );
    }
}