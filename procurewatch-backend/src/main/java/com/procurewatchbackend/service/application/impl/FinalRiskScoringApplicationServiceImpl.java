package com.procurewatchbackend.service.application.impl;

import com.procurewatchbackend.dto.display.GetRiskAssessmentDto;
import com.procurewatchbackend.dto.display.GetTriggeredRiskFlagDto;
import com.procurewatchbackend.model.entity.RiskAssessment;
import com.procurewatchbackend.model.entity.TriggeredRiskFlag;
import com.procurewatchbackend.service.application.FinalRiskScoringApplicationService;
import com.procurewatchbackend.service.domain.FinalRiskScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinalRiskScoringApplicationServiceImpl implements FinalRiskScoringApplicationService {

    private final FinalRiskScoringService finalRiskScoringService;

    @Override
    @Transactional
    public GetRiskAssessmentDto recalculateContract(Long contractId) {
        return mapToDto(finalRiskScoringService.recalculateExistingAssessment(contractId));
    }

    @Override
    @Transactional
    public List<GetRiskAssessmentDto> recalculateAll() {
        return finalRiskScoringService.recalculateAllExistingAssessments()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private GetRiskAssessmentDto mapToDto(RiskAssessment assessment) {
        return new GetRiskAssessmentDto(
                assessment.getId(),
                assessment.getContract().getId(),
                assessment.getRuleScore(),
                assessment.getAnomalyScore(),
                assessment.getSimilarityScore(),
                assessment.getClusterScore(),
                assessment.getFinalRiskScore(),
                assessment.getRiskLevel(),
                assessment.getPriorityRank(),
                assessment.getModelVersion(),
                assessment.getEvaluatedAt(),
                assessment.getTriggeredFlags()
                        .stream()
                        .map(this::mapFlag)
                        .toList()
        );
    }

    private GetTriggeredRiskFlagDto mapFlag(TriggeredRiskFlag flag) {
        return new GetTriggeredRiskFlagDto(
                flag.getId(),
                flag.getFlagCode(),
                flag.getFlagName(),
                flag.getFlagDescription(),
                flag.getWeight(),
                flag.getMeasuredValue(),
                flag.getThresholdValue(),
                flag.getCreatedAt()
        );
    }
}