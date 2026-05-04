package com.procurewatchbackend.service.application;

import com.procurewatchbackend.dto.display.GetRiskAssessmentDto;

import java.util.List;

public interface FinalRiskScoringApplicationService {

    GetRiskAssessmentDto recalculateContract(Long contractId);

    List<GetRiskAssessmentDto> recalculateAll();
}