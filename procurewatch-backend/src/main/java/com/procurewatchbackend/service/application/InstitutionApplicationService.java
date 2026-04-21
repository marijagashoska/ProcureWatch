package com.procurewatchbackend.service.application;

import com.procurewatchbackend.dto.create.CreateInstitutionDto;
import com.procurewatchbackend.dto.display.GetInstitutionDto;
import com.procurewatchbackend.dto.edit.EditInstitutionDto;

import java.util.List;

public interface InstitutionApplicationService {

    GetInstitutionDto add(CreateInstitutionDto dto);

    GetInstitutionDto edit(Long id, EditInstitutionDto dto);

    void delete(Long id);

    List<GetInstitutionDto> getAll();

    GetInstitutionDto getById(Long id);

    List<GetInstitutionDto> getByCity(String city);

    List<GetInstitutionDto> getByInstType(String institutionType);

    List<GetInstitutionDto> getByCategory(String category);

    List<GetInstitutionDto> getByOfficialName(String officialName);

    List<GetInstitutionDto> getByNormalizedName(String normalizedName);
}