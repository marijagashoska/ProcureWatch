package com.procurewatchbackend.service.application.impl;

import com.procurewatchbackend.dto.create.CreateInstitutionDto;
import com.procurewatchbackend.dto.display.GetInstitutionDto;
import com.procurewatchbackend.dto.edit.EditInstitutionDto;
import com.procurewatchbackend.model.entity.Institution;
import com.procurewatchbackend.service.application.InstitutionApplicationService;
import com.procurewatchbackend.service.domain.InstitutionDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstitutionApplicationServiceImpl implements InstitutionApplicationService {

    private final InstitutionDomainService institutionDomainService;

    @Override
    @Transactional
    public GetInstitutionDto add(CreateInstitutionDto dto) {
        return mapToGetDto(institutionDomainService.add(dto));
    }

    @Override
    @Transactional
    public GetInstitutionDto edit(Long id, EditInstitutionDto dto) {
        return mapToGetDto(institutionDomainService.edit(id, dto));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        institutionDomainService.delete(id);
    }

    @Override
    public List<GetInstitutionDto> getAll() {
        return institutionDomainService.getAll()
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public GetInstitutionDto getById(Long id) {
        return mapToGetDto(institutionDomainService.getById(id));
    }

    @Override
    public List<GetInstitutionDto> getByCity(String city) {
        return institutionDomainService.getByCity(city)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetInstitutionDto> getByInstType(String institutionType) {
        return institutionDomainService.getByInstType(institutionType)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetInstitutionDto> getByCategory(String category) {
        return institutionDomainService.getByCategory(category)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetInstitutionDto> getByOfficialName(String officialName) {
        return institutionDomainService.getByOfficialName(officialName)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetInstitutionDto> getByNormalizedName(String normalizedName) {
        return institutionDomainService.getByNormalizedName(normalizedName)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    private GetInstitutionDto mapToGetDto(Institution institution) {
        return new GetInstitutionDto(
                institution.getId(),
                institution.getExternalId(),
                institution.getOfficialName(),
                institution.getNormalizedName(),
                institution.getInstitutionType(),
                institution.getCity(),
                institution.getPostalCode(),
                institution.getCategory(),
                institution.getSourceUrl()
        );
    }
}