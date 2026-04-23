package com.procurewatchbackend.service.application.impl;

import com.procurewatchbackend.dto.create.CreateProcurementPlanDto;
import com.procurewatchbackend.dto.display.GetPlanItemDto;
import com.procurewatchbackend.dto.display.GetProcurementPlanDto;
import com.procurewatchbackend.dto.display.PagedResponseDto;
import com.procurewatchbackend.dto.edit.EditProcurementPlanDto;
import com.procurewatchbackend.model.entity.PlanItem;
import com.procurewatchbackend.model.entity.ProcurementPlan;
import com.procurewatchbackend.service.application.ProcurementPlanApplicationService;
import com.procurewatchbackend.service.domain.ProcurementPlanDomainService;
import com.procurewatchbackend.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProcurementPlanApplicationServiceImpl implements ProcurementPlanApplicationService {

    private final ProcurementPlanDomainService procurementPlanDomainService;

    @Override
    @Transactional
    public GetProcurementPlanDto add(CreateProcurementPlanDto dto) {
        return mapToGetDto(procurementPlanDomainService.add(dto));
    }

    @Override
    @Transactional
    public GetProcurementPlanDto edit(Long id, EditProcurementPlanDto dto) {
        return mapToGetDto(procurementPlanDomainService.edit(id, dto));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        procurementPlanDomainService.delete(id);
    }

    @Override
    public List<GetProcurementPlanDto> getAll() {
        return procurementPlanDomainService.getAll()
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public GetProcurementPlanDto getById(Long id) {
        return mapToGetDto(procurementPlanDomainService.getById(id));
    }

    @Override
    public List<GetProcurementPlanDto> getByInstitutionId(Long institutionId) {
        return procurementPlanDomainService.getByInstitutionId(institutionId)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetProcurementPlanDto> getByYear(Integer year) {
        return procurementPlanDomainService.getByYear(year)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    private GetProcurementPlanDto mapToGetDto(ProcurementPlan plan) {
        List<GetPlanItemDto> planItemDtos = plan.getPlanItems() == null
                ? Collections.emptyList()
                : plan.getPlanItems().stream()
                .map(this::mapPlanItemToGetDto)
                .toList();

        return new GetProcurementPlanDto(
                plan.getId(),
                plan.getInstitution().getId(),
                plan.getPlanYear(),
                plan.getPublicationDate(),
                plan.getSourceUrl(),
                planItemDtos
        );
    }

    private GetPlanItemDto mapPlanItemToGetDto(PlanItem item) {
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

    @Override
    public PagedResponseDto<GetProcurementPlanDto> getAllPaginated(
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Pageable pageable = PageUtils.createPageable(page, size, sortBy, sortDir);

        Page<GetProcurementPlanDto> result = procurementPlanDomainService.getAllPaginated(pageable)
                .map(this::mapToGetDto);

        return PageUtils.toPagedResponse(result, sortBy, sortDir);
    }
}