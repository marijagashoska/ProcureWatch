package com.procurewatchbackend.service.application;

import com.procurewatchbackend.dto.create.CreatePlanItemDto;
import com.procurewatchbackend.dto.display.GetPlanItemDto;
import com.procurewatchbackend.dto.edit.EditPlanItemDto;

import java.util.List;

public interface PlanItemApplicationService {

    GetPlanItemDto add(CreatePlanItemDto dto);

    GetPlanItemDto edit(Long id, EditPlanItemDto dto);

    void delete(Long id);

    List<GetPlanItemDto> getAll();

    GetPlanItemDto getById(Long id);

    List<GetPlanItemDto> getByPlanId(Long planId);
}