package com.procurewatchbackend.repository;

import com.procurewatchbackend.model.entity.RiskAssessment;
import com.procurewatchbackend.model.enums.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {

    Optional<RiskAssessment> findByContractId(Long contractId);

    List<RiskAssessment> findByRiskLevelOrderByFinalRiskScoreDesc(RiskLevel riskLevel);

    long countByFinalRiskScoreGreaterThan(BigDecimal score);
}