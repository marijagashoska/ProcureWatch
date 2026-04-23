package com.procurewatchbackend.service.domain;

import com.procurewatchbackend.model.entity.RiskAssessment;
import com.procurewatchbackend.model.enums.RiskLevel;

import java.util.List;

public interface RiskAssessmentDomainService {

    RiskAssessment evaluateContract(Long contractId);

    List<RiskAssessment> evaluateAllContracts();

    RiskAssessment getByContractId(Long contractId);

    List<RiskAssessment> getByRiskLevel(RiskLevel riskLevel);
}