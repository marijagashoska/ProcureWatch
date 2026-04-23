package com.procurewatchbackend.service.application.impl;

import com.procurewatchbackend.dto.display.GetRiskAssessmentDto;
import com.procurewatchbackend.dto.display.GetTriggeredRiskFlagDto;
import com.procurewatchbackend.model.entity.RiskAssessment;
import com.procurewatchbackend.model.entity.TriggeredRiskFlag;
import com.procurewatchbackend.model.enums.RiskLevel;
import com.procurewatchbackend.service.application.RiskAssessmentApplicationService;
import com.procurewatchbackend.service.domain.RiskAssessmentDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiskAssessmentApplicationServiceImpl implements RiskAssessmentApplicationService {

    private final RiskAssessmentDomainService riskAssessmentDomainService;

    @Override
    @Transactional
    public GetRiskAssessmentDto evaluateContract(Long contractId) {
        return mapToDto(riskAssessmentDomainService.evaluateContract(contractId));
    }

    @Override
    @Transactional
    public List<GetRiskAssessmentDto> evaluateAllContracts() {
        return riskAssessmentDomainService.evaluateAllContracts()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public GetRiskAssessmentDto getByContractId(Long contractId) {
        return mapToDto(riskAssessmentDomainService.getByContractId(contractId));
    }

    @Override
    public List<GetRiskAssessmentDto> getByRiskLevel(RiskLevel riskLevel) {
        return riskAssessmentDomainService.getByRiskLevel(riskLevel)
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
                assessment.getTriggeredFlags().stream()
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