package com.procurewatchbackend.service.domain;

import com.procurewatchbackend.dto.create.CreateInstitutionDto;
import com.procurewatchbackend.dto.edit.EditInstitutionDto;
import com.procurewatchbackend.model.entity.Institution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InstitutionDomainService {

    Institution add(CreateInstitutionDto dto);

    Institution edit(Long id, EditInstitutionDto dto);

    void delete(Long id);

    List<Institution> getAll();

    Institution getById(Long id);

    List<Institution> getByCity(String city);

    List<Institution> getByInstType(String institutionType);

    List<Institution> getByCategory(String category);

    List<Institution> getByOfficialName(String officialName);

    List<Institution> getByNormalizedName(String normalizedName);
    Page<Institution> getAllPaginated(Pageable pageable);
}