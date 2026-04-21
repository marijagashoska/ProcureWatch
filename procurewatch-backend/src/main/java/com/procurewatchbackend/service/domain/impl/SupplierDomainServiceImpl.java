package com.procurewatchbackend.service.domain.impl;

import com.procurewatchbackend.dto.create.CreateSupplierDto;
import com.procurewatchbackend.dto.edit.EditSupplierDto;
import com.procurewatchbackend.model.entity.Supplier;
import com.procurewatchbackend.repository.DecisionRepository;
import com.procurewatchbackend.repository.SupplierRepository;
import com.procurewatchbackend.service.domain.SupplierDomainService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierDomainServiceImpl implements SupplierDomainService {

    private final SupplierRepository supplierRepository;
    private final DecisionRepository decisionRepository;

    @Override
    public Supplier add(CreateSupplierDto dto) {
        Supplier supplier = new Supplier();
        supplier.setExternalId(dto.externalId());
        supplier.setOfficialName(dto.officialName());
        supplier.setNormalizedName(dto.normalizedName());
        supplier.setRealOwnersInfo(dto.realOwnersInfo());
        supplier.setSourceUrl(dto.sourceUrl());

        Supplier saved = supplierRepository.save(supplier);
        attachDecisions(saved);

        return saved;
    }

    @Override
    public Supplier edit(Long id, EditSupplierDto dto) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Supplier not found with id: " + id
                ));

        existing.setExternalId(dto.externalId());
        existing.setOfficialName(dto.officialName());
        existing.setNormalizedName(dto.normalizedName());
        existing.setRealOwnersInfo(dto.realOwnersInfo());
        existing.setSourceUrl(dto.sourceUrl());

        Supplier updated = supplierRepository.save(existing);
        attachDecisions(updated);

        return updated;
    }

    @Override
    public void delete(Long id) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Supplier not found with id: " + id
                ));

        supplierRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> getAll() {
        List<Supplier> suppliers = supplierRepository.findAll();
        suppliers.forEach(this::attachDecisions);
        return suppliers;
    }

    @Override
    @Transactional(readOnly = true)
    public Supplier getById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Supplier not found with id: " + id
                ));

        attachDecisions(supplier);
        return supplier;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> getByOfficialName(String officialName) {
        List<Supplier> suppliers = supplierRepository.findByOfficialNameContainingIgnoreCase(officialName);
        suppliers.forEach(this::attachDecisions);
        return suppliers;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> getByNormalizedName(String normalizedName) {
        List<Supplier> suppliers = supplierRepository.findByNormalizedNameContainingIgnoreCase(normalizedName);
        suppliers.forEach(this::attachDecisions);
        return suppliers;
    }

    @Override
    @Transactional(readOnly = true)
    public Supplier getByExternalId(String externalId) {
        Supplier supplier = supplierRepository.findByExternalId(externalId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Supplier not found with externalId: " + externalId
                ));

        attachDecisions(supplier);
        return supplier;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> getByRealOwnersInfo(String realOwnersInfo) {
        List<Supplier> suppliers = supplierRepository.findByRealOwnersInfoContainingIgnoreCase(realOwnersInfo);
        suppliers.forEach(this::attachDecisions);
        return suppliers;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> allThatHaveDecision() {
        List<Supplier> suppliers = supplierRepository.findAllThatHaveDecision();
        suppliers.forEach(this::attachDecisions);
        return suppliers;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> allThatDontHaveDecision() {
        List<Supplier> suppliers = supplierRepository.findAllThatDontHaveDecision();
        suppliers.forEach(this::attachDecisions);
        return suppliers;
    }

    private void attachDecisions(Supplier supplier) {
        supplier.setDecisions(decisionRepository.findBySupplierId(supplier.getId()));
    }
}