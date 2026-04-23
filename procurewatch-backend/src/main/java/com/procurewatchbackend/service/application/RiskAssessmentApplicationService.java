package com.procurewatchbackend.service.application;

import com.procurewatchbackend.dto.display.GetRiskAssessmentDto;
import com.procurewatchbackend.model.enums.RiskLevel;

import java.util.List;

public interface RiskAssessmentApplicationService {

    GetRiskAssessmentDto evaluateContract(Long contractId);

    List<GetRiskAssessmentDto> evaluateAllContracts();

    GetRiskAssessmentDto getByContractId(Long contractId);

    List<GetRiskAssessmentDto> getByRiskLevel(RiskLevel riskLevel);
}