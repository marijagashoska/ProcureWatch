package com.procurewatchbackend.service.domain.impl;

import com.procurewatchbackend.dto.create.CreateInstitutionDto;
import com.procurewatchbackend.dto.edit.EditInstitutionDto;
import com.procurewatchbackend.model.entity.Institution;
import com.procurewatchbackend.repository.InstitutionRepository;
import com.procurewatchbackend.service.domain.InstitutionDomainService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InstitutionDomainServiceImpl implements InstitutionDomainService {

    private final InstitutionRepository institutionRepository;

    @Override
    public Institution add(CreateInstitutionDto dto) {
        Institution institution = new Institution();
        institution.setExternalId(dto.externalId());
        institution.setOfficialName(dto.officialName());
        institution.setNormalizedName(dto.normalizedName());
        institution.setInstitutionType(dto.institutionType());
        institution.setCity(dto.city());
        institution.setPostalCode(dto.postalCode());
        institution.setCategory(dto.category());
        institution.setSourceUrl(dto.sourceUrl());

        return institutionRepository.save(institution);
    }

    @Override
    public Institution edit(Long id, EditInstitutionDto dto) {
        Institution existing = institutionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Institution not found with id: " + id
                ));

        existing.setExternalId(dto.externalId());
        existing.setOfficialName(dto.officialName());
        existing.setNormalizedName(dto.normalizedName());
        existing.setInstitutionType(dto.institutionType());
        existing.setCity(dto.city());
        existing.setPostalCode(dto.postalCode());
        existing.setCategory(dto.category());
        existing.setSourceUrl(dto.sourceUrl());

        return institutionRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Institution existing = institutionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Institution not found with id: " + id
                ));

        institutionRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Institution> getAll() {
        return institutionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Institution getById(Long id) {
        return institutionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Institution not found with id: " + id
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Institution> getByCity(String city) {
        return institutionRepository.findByCityContainingIgnoreCase(city);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Institution> getByInstType(String institutionType) {
        return institutionRepository.findByInstitutionTypeIgnoreCase(institutionType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Institution> getByCategory(String category) {
        return institutionRepository.findByCategoryIgnoreCase(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Institution> getByOfficialName(String officialName) {
        return institutionRepository.findByOfficialNameContainingIgnoreCase(officialName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Institution> getByNormalizedName(String normalizedName) {
        return institutionRepository.findByNormalizedNameContainingIgnoreCase(normalizedName);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Institution> getAllPaginated(Pageable pageable) {
        return institutionRepository.findAll(pageable);
    }
}