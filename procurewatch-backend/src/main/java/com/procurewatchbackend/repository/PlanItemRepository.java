package com.procurewatchbackend.repository;

import com.procurewatchbackend.model.entity.PlanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanItemRepository extends JpaRepository<PlanItem,Long> {

    List<PlanItem> findByPlanId(Long planId);
    void deleteByPlanId(Long planId);
    Optional<PlanItem> findFirstByPlanIdAndSubjectIgnoreCaseAndCpvCode(
            Long planId,
            String subject,
            String cpvCode
    );

    Optional<PlanItem> findFirstByPlan_Institution_IdAndSubjectContainingIgnoreCaseAndPlan_PlanYear(
            Long institutionId,
            String subject,
            Integer planYear
    );
}
