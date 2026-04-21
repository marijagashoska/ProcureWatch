package com.procurewatchbackend.service.domain;

import com.procurewatchbackend.dto.create.CreateProcurementPlanDto;
import com.procurewatchbackend.dto.edit.EditProcurementPlanDto;
import com.procurewatchbackend.model.entity.ProcurementPlan;

import java.util.List;

public interface ProcurementPlanDomainService {

    ProcurementPlan add(CreateProcurementPlanDto dto);

    ProcurementPlan edit(Long id, EditProcurementPlanDto dto);

    void delete(Long id);

    List<ProcurementPlan> getAll();

    ProcurementPlan getById(Long id);

    List<ProcurementPlan> getByInstitutionId(Long institutionId);

    List<ProcurementPlan> getByYear(Integer year);

}
