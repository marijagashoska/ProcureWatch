package com.procurewatchbackend.service.domain;

import com.procurewatchbackend.model.entity.RiskAssessment;
import com.procurewatchbackend.model.enums.RiskLevel;

import java.math.BigDecimal;
import java.util.List;

public interface FinalRiskScoringService {

    RiskAssessment calculateAndApply(RiskAssessment assessment);

    RiskAssessment recalculateExistingAssessment(Long contractId);

    List<RiskAssessment> recalculateAllExistingAssessments();

    void recalculatePriorityRanks();

    RiskLevel resolveRiskLevel(BigDecimal finalRiskScore);
}