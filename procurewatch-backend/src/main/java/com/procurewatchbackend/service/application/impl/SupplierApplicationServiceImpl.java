package com.procurewatchbackend.service.application.impl;

import com.procurewatchbackend.dto.create.CreateSupplierDto;
import com.procurewatchbackend.dto.display.GetSupplierDto;
import com.procurewatchbackend.dto.edit.EditSupplierDto;
import com.procurewatchbackend.model.entity.Decision;
import com.procurewatchbackend.model.entity.Supplier;
import com.procurewatchbackend.service.application.SupplierApplicationService;
import com.procurewatchbackend.service.domain.SupplierDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierApplicationServiceImpl implements SupplierApplicationService {

    private final SupplierDomainService supplierDomainService;

    @Override
    @Transactional
    public GetSupplierDto add(CreateSupplierDto dto) {
        return mapToGetDto(supplierDomainService.add(dto));
    }

    @Override
    @Transactional
    public GetSupplierDto edit(Long id, EditSupplierDto dto) {
        return mapToGetDto(supplierDomainService.edit(id, dto));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        supplierDomainService.delete(id);
    }

    @Override
    public List<GetSupplierDto> getAll() {
        return supplierDomainService.getAll()
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public GetSupplierDto getById(Long id) {
        return mapToGetDto(supplierDomainService.getById(id));
    }

    @Override
    public List<GetSupplierDto> getByOfficialName(String officialName) {
        return supplierDomainService.getByOfficialName(officialName)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetSupplierDto> getByNormalizedName(String normalizedName) {
        return supplierDomainService.getByNormalizedName(normalizedName)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public GetSupplierDto getByExternalId(String externalId) {
        return mapToGetDto(supplierDomainService.getByExternalId(externalId));
    }

    @Override
    public List<GetSupplierDto> getByRealOwnersInfo(String realOwnersInfo) {
        return supplierDomainService.getByRealOwnersInfo(realOwnersInfo)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetSupplierDto> allThatHaveDecision() {
        return supplierDomainService.allThatHaveDecision()
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetSupplierDto> allThatDontHaveDecision() {
        return supplierDomainService.allThatDontHaveDecision()
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    private GetSupplierDto mapToGetDto(Supplier supplier) {
        List<Long> decisionIds = supplier.getDecisions() == null
                ? Collections.emptyList()
                : supplier.getDecisions().stream().map(Decision::getId).toList();

        return new GetSupplierDto(
                supplier.getId(),
                supplier.getExternalId(),
                supplier.getOfficialName(),
                supplier.getNormalizedName(),
                supplier.getRealOwnersInfo(),
                supplier.getSourceUrl(),
                decisionIds
        );
    }
}