package com.procurewatchbackend.repository;

import com.procurewatchbackend.model.entity.ProcurementPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcurementPlanRepository extends JpaRepository<ProcurementPlan, Long> {

    List<ProcurementPlan> findByInstitutionId(Long institutionId);
    List<ProcurementPlan> findByPlanYear(Integer year);
    Optional<ProcurementPlan> findFirstByInstitutionIdAndPlanYear(Long institutionId, Integer planYear);
}
