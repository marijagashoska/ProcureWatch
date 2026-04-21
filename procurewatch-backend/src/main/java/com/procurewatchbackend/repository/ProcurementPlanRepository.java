package com.procurewatchbackend.repository;

import com.procurewatchbackend.model.entity.ProcurementPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcurementPlanRepository extends JpaRepository<ProcurementPlan, Long> {
}
