package com.procurewatchbackend.service.application.impl;

import com.procurewatchbackend.dto.display.GetContractDto;
import com.procurewatchbackend.dto.display.GetNoticeDto;
import com.procurewatchbackend.dto.display.GetPlanItemDto;
import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.model.entity.Notice;
import com.procurewatchbackend.model.entity.PlanItem;
import com.procurewatchbackend.service.application.TextSimilarityApplicationService;
import com.procurewatchbackend.service.domain.TextSimilarityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TextSimilarityApplicationServiceImpl implements TextSimilarityApplicationService {

    private final TextSimilarityService textSimilarityService;

    @Override
    public double compareTexts(String text1, String text2) {
        return textSimilarityService.compareTexts(text1, text2);
    }

    @Override
    public List<GetContractDto> findSimilarContracts(Long contractId) {
        List<Contract> contracts = textSimilarityService.findSimilarContracts(contractId);
        return contracts.stream()
                .map(this::mapContractToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GetNoticeDto> findSimilarNotices(Long noticeId) {
        List<Notice> notices = textSimilarityService.findSimilarNotices(noticeId);
        return notices.stream()
                .map(this::mapNoticeToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GetPlanItemDto> findSimilarPlanItems(Long planItemId) {
        List<PlanItem> planItems = textSimilarityService.findSimilarPlanItems(planItemId);
        return planItems.stream()
                .map(this::mapPlanItemToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> findSimilarDocuments(Long contractId) {
        Map<String, List<?>> documents = textSimilarityService.findSimilarDocuments(contractId);
        Map<String, Object> result = new HashMap<>();

        if (documents.containsKey("contracts")) {
            result.put("contracts", ((List<?>) documents.get("contracts")).stream()
                    .map(c -> mapContractToDto((Contract) c))
                    .collect(Collectors.toList()));
        }

        if (documents.containsKey("notices")) {
            result.put("notices", ((List<?>) documents.get("notices")).stream()
                    .map(n -> mapNoticeToDto((Notice) n))
                    .collect(Collectors.toList()));
        }

        if (documents.containsKey("planItems")) {
            result.put("planItems", ((List<?>) documents.get("planItems")).stream()
                    .map(p -> mapPlanItemToDto((PlanItem) p))
                    .collect(Collectors.toList()));
        }

        return result;
    }

    @Override
    public SimilarityScoreDto calculateContractSimilarityScore(Long contractId) {
        double score = textSimilarityService.calculateContractSimilarityScore(contractId);
        return new SimilarityScoreDto(score, String.format("%.2f%%", score * 100));
    }

    private GetContractDto mapContractToDto(Contract contract) {
        return new GetContractDto(
                contract.getId(),
                contract.getInstitution() != null ? contract.getInstitution().getId() : null,
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

    private GetNoticeDto mapNoticeToDto(Notice notice) {
        return new GetNoticeDto(
                notice.getId(),
                notice.getInstitution() != null ? notice.getInstitution().getId() : null,
                notice.getPlanItem() != null ? notice.getPlanItem().getId() : null,
                notice.getDecisions() != null ? notice.getDecisions().stream()
                        .map(d -> d.getId())
                        .collect(Collectors.toList()) : List.of(),
                notice.getNoticeNumber(),
                notice.getSubject(),
                notice.getContractType(),
                notice.getProcedureType(),
                notice.getPublicationDate(),
                notice.getDeadlineDate(),
                notice.getSourceUrl()
        );
    }

    private GetPlanItemDto mapPlanItemToDto(PlanItem planItem) {
        return new GetPlanItemDto(
                planItem.getId(),
                planItem.getSubject(),
                planItem.getCpvCode(),
                planItem.getContractType(),
                planItem.getProcedureType(),
                planItem.getExpectedStartMonth(),
                planItem.getHasNotice(),
                planItem.getNotes(),
                planItem.getSourceUrl()
        );
    }
}

