package com.procurewatchbackend.service.domain;

import com.procurewatchbackend.dto.create.CreatePlanItemDto;
import com.procurewatchbackend.dto.edit.EditPlanItemDto;
import com.procurewatchbackend.model.entity.PlanItem;

import java.util.List;

public interface PlanItemDomainService {

    PlanItem add(CreatePlanItemDto dto);

    PlanItem edit(Long id, EditPlanItemDto dto);

    void delete(Long id);

    List<PlanItem> getAll();

    PlanItem getById(Long id);

    List<PlanItem> getByPlanId(Long planId);
}