package com.procurewatchbackend.service.application.impl;

import com.procurewatchbackend.dto.display.GetAIExplanationDto;
import com.procurewatchbackend.model.entity.AIExplanation;
import com.procurewatchbackend.service.application.AIExplanationApplicationService;
import com.procurewatchbackend.service.domain.AIExplanationDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AIExplanationApplicationServiceImpl implements AIExplanationApplicationService {

    private final AIExplanationDomainService aiExplanationDomainService;

    @Override
    @Transactional
    public GetAIExplanationDto generateForContract(Long contractId) {
        return mapToDto(aiExplanationDomainService.generateForContract(contractId));
    }

    @Override
    public GetAIExplanationDto getLatestByContractId(Long contractId) {
        return mapToDto(aiExplanationDomainService.getLatestByContractId(contractId));
    }

    @Override
    public List<GetAIExplanationDto> getHistoryByContractId(Long contractId) {
        return aiExplanationDomainService.getHistoryByContractId(contractId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private GetAIExplanationDto mapToDto(AIExplanation explanation) {
        return new GetAIExplanationDto(
                explanation.getId(),
                explanation.getContract() != null ? explanation.getContract().getId() : null,
                explanation.getRiskAssessment() != null ? explanation.getRiskAssessment().getId() : null,
                explanation.getSummaryText(),
                explanation.getExplanationText(),
                explanation.getRecommendationText(),
                explanation.getGeneratorType(),
                explanation.getModelVersion(),
                explanation.getGeneratedAt()
        );
    }
}