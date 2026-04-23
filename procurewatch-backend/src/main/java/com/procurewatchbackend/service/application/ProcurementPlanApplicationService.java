package com.procurewatchbackend.service.application;

import com.procurewatchbackend.dto.create.CreateProcurementPlanDto;
import com.procurewatchbackend.dto.display.GetInstitutionDto;
import com.procurewatchbackend.dto.display.GetProcurementPlanDto;
import com.procurewatchbackend.dto.display.PagedResponseDto;
import com.procurewatchbackend.dto.edit.EditProcurementPlanDto;

import java.util.List;

public interface ProcurementPlanApplicationService {

    GetProcurementPlanDto add(CreateProcurementPlanDto dto);

    GetProcurementPlanDto edit(Long id, EditProcurementPlanDto dto);

    void delete(Long id);

    List<GetProcurementPlanDto> getAll();

    GetProcurementPlanDto getById(Long id);

    List<GetProcurementPlanDto> getByInstitutionId(Long institutionId);

    List<GetProcurementPlanDto> getByYear(Integer year);

    PagedResponseDto<GetProcurementPlanDto> getAllPaginated(
            int page,
            int size,
            String sortBy,
            String sortDir
    );
}