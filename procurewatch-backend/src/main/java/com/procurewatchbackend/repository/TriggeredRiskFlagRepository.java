package com.procurewatchbackend.repository;

import com.procurewatchbackend.model.entity.TriggeredRiskFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TriggeredRiskFlagRepository extends JpaRepository<TriggeredRiskFlag, Long> {

    List<TriggeredRiskFlag> findByRiskAssessmentIdOrderByWeightDesc(Long riskAssessmentId);
}