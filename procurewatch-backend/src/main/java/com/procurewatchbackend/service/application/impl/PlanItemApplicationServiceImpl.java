package com.procurewatchbackend.service.application.impl;

import com.procurewatchbackend.dto.create.CreatePlanItemDto;
import com.procurewatchbackend.dto.display.GetPlanItemDto;
import com.procurewatchbackend.dto.edit.EditPlanItemDto;
import com.procurewatchbackend.model.entity.PlanItem;
import com.procurewatchbackend.service.application.PlanItemApplicationService;
import com.procurewatchbackend.service.domain.PlanItemDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanItemApplicationServiceImpl implements PlanItemApplicationService {

    private final PlanItemDomainService planItemDomainService;

    @Override
    @Transactional
    public GetPlanItemDto add(CreatePlanItemDto dto) {
        return mapToGetDto(planItemDomainService.add(dto));
    }

    @Override
    @Transactional
    public GetPlanItemDto edit(Long id, EditPlanItemDto dto) {
        return mapToGetDto(planItemDomainService.edit(id, dto));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        planItemDomainService.delete(id);
    }

    @Override
    public List<GetPlanItemDto> getAll() {
        return planItemDomainService.getAll()
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public GetPlanItemDto getById(Long id) {
        return mapToGetDto(planItemDomainService.getById(id));
    }

    @Override
    public List<GetPlanItemDto> getByPlanId(Long planId) {
        return planItemDomainService.getByPlanId(planId)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    private GetPlanItemDto mapToGetDto(PlanItem item) {
        return new GetPlanItemDto(
                item.getId(),
                item.getSubject(),
                item.getCpvCode(),
                item.getContractType(),
                item.getProcedureType(),
                item.getExpectedStartMonth(),
                item.getHasNotice(),
                item.getNotes(),
                item.getSourceUrl()
        );
    }
}