package com.procurewatchbackend.service.application.impl;

import com.procurewatchbackend.dto.create.CreateInstitutionDto;
import com.procurewatchbackend.dto.display.GetInstitutionDto;
import com.procurewatchbackend.dto.display.PagedResponseDto;
import com.procurewatchbackend.dto.edit.EditInstitutionDto;
import com.procurewatchbackend.model.entity.Institution;
import com.procurewatchbackend.service.application.InstitutionApplicationService;
import com.procurewatchbackend.service.domain.InstitutionDomainService;
import com.procurewatchbackend.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Override
    public PagedResponseDto<GetInstitutionDto> getAllPaginated(
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Pageable pageable = PageUtils.createPageable(page, size, sortBy, sortDir);

        Page<GetInstitutionDto> result = institutionDomainService.getAllPaginated(pageable)
                .map(this::mapToGetDto);

        return PageUtils.toPagedResponse(result, sortBy, sortDir);
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