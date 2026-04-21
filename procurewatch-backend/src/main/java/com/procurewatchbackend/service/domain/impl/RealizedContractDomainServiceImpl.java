package com.procurewatchbackend.service.domain.impl;

import com.procurewatchbackend.dto.create.CreateRealizedContractDto;
import com.procurewatchbackend.dto.edit.EditRealizedContractDto;
import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.model.entity.Institution;
import com.procurewatchbackend.model.entity.RealizedContract;
import com.procurewatchbackend.model.entity.Supplier;
import com.procurewatchbackend.repository.ContractRepository;
import com.procurewatchbackend.repository.InstitutionRepository;
import com.procurewatchbackend.repository.RealizedContractRepository;
import com.procurewatchbackend.repository.SupplierRepository;
import com.procurewatchbackend.service.domain.RealizedContractDomainService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RealizedContractDomainServiceImpl implements RealizedContractDomainService {

    private final RealizedContractRepository realizedContractRepository;
    private final InstitutionRepository institutionRepository;
    private final SupplierRepository supplierRepository;
    private final ContractRepository contractRepository;

    @Override
    public RealizedContract add(CreateRealizedContractDto dto) {
        Institution institution = resolveInstitution(dto.institutionId());
        Supplier supplier = resolveSupplier(dto.supplierId());
        Contract contract = resolveContract(dto.contractId());

        RealizedContract realizedContract = new RealizedContract();
        realizedContract.setInstitution(institution);
        realizedContract.setSupplier(supplier);
        realizedContract.setContract(contract);
        realizedContract.setNoticeNumber(dto.noticeNumber());
        realizedContract.setSubject(dto.subject());
        realizedContract.setContractType(dto.contractType());
        realizedContract.setProcedureType(dto.procedureType());
        realizedContract.setAwardedValueVat(dto.awardedValueVat());
        realizedContract.setRealizedValueVat(dto.realizedValueVat());
        realizedContract.setPaidValueVat(dto.paidValueVat());
        realizedContract.setPublicationDate(dto.publicationDate());
        realizedContract.setRepublishDate(dto.republishDate());
        realizedContract.setSourceUrl(dto.sourceUrl());

        return realizedContractRepository.save(realizedContract);
    }

    @Override
    public RealizedContract edit(Long id, EditRealizedContractDto dto) {
        RealizedContract existing = realizedContractRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "RealizedContract not found with id: " + id
                ));

        Institution institution = resolveInstitution(dto.institutionId());
        Supplier supplier = resolveSupplier(dto.supplierId());
        Contract contract = resolveContract(dto.contractId());

        existing.setInstitution(institution);
        existing.setSupplier(supplier);
        existing.setContract(contract);
        existing.setNoticeNumber(dto.noticeNumber());
        existing.setSubject(dto.subject());
        existing.setContractType(dto.contractType());
        existing.setProcedureType(dto.procedureType());
        existing.setAwardedValueVat(dto.awardedValueVat());
        existing.setRealizedValueVat(dto.realizedValueVat());
        existing.setPaidValueVat(dto.paidValueVat());
        existing.setPublicationDate(dto.publicationDate());
        existing.setRepublishDate(dto.republishDate());
        existing.setSourceUrl(dto.sourceUrl());

        return realizedContractRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        RealizedContract existing = realizedContractRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "RealizedContract not found with id: " + id
                ));

        realizedContractRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealizedContract> getAllByInstitution(Long institutionId) {
        return realizedContractRepository.findByInstitutionId(institutionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealizedContract> getAllBySupplier(Long supplierId) {
        return realizedContractRepository.findBySupplierId(supplierId);
    }

    @Override
    @Transactional(readOnly = true)
    public RealizedContract getByContract(Long contractId) {
        return realizedContractRepository.findByContractId(contractId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "RealizedContract not found for contract id: " + contractId
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealizedContract> getByContractType(String contractType) {
        return realizedContractRepository.findByContractTypeIgnoreCase(contractType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealizedContract> getBySubject(String subject) {
        return realizedContractRepository.findBySubjectContainingIgnoreCase(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealizedContract> getByProcedureType(String procedureType) {
        return realizedContractRepository.findByProcedureTypeIgnoreCase(procedureType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealizedContract> getByPublicationDate(LocalDate publicationDate) {
        return realizedContractRepository.findByPublicationDate(publicationDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealizedContract> getByRepublishDate(LocalDate republishDate) {
        return realizedContractRepository.findByRepublishDate(republishDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealizedContract> getByAwardedValueVatGreaterThanEqual(BigDecimal value) {
        return realizedContractRepository.findByAwardedValueVatGreaterThanEqual(value);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealizedContract> getByAwardedValueVatLessThanEqual(BigDecimal value) {
        return realizedContractRepository.findByAwardedValueVatLessThanEqual(value);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealizedContract> getByAwardedValueVatBetween(BigDecimal min, BigDecimal max) {
        return realizedContractRepository.findByAwardedValueVatBetween(min, max);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealizedContract> getByRealizedValueVatGreaterThanEqual(BigDecimal value) {
        return realizedContractRepository.findByRealizedValueVatGreaterThanEqual(value);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealizedContract> getByRealizedValueVatLessThanEqual(BigDecimal value) {
        return realizedContractRepository.findByRealizedValueVatLessThanEqual(value);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealizedContract> getByRealizedValueVatBetween(BigDecimal min, BigDecimal max) {
        return realizedContractRepository.findByRealizedValueVatBetween(min, max);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealizedContract> getByPaidValueVatGreaterThanEqual(BigDecimal value) {
        return realizedContractRepository.findByPaidValueVatGreaterThanEqual(value);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealizedContract> getByPaidValueVatLessThanEqual(BigDecimal value) {
        return realizedContractRepository.findByPaidValueVatLessThanEqual(value);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealizedContract> getByPaidValueVatBetween(BigDecimal min, BigDecimal max) {
        return realizedContractRepository.findByPaidValueVatBetween(min, max);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealizedContract> allWhereRealizedValueExceedsAwardedValue() {
        return realizedContractRepository.findAllWhereRealizedValueExceedsAwardedValue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealizedContract> allWherePaidValueExceedsRealizedValue() {
        return realizedContractRepository.findAllWherePaidValueExceedsRealizedValue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealizedContract> allWherePaidValueIsLessThanOrEqualToRealizedValue() {
        return realizedContractRepository.findAllWherePaidValueIsLessThanOrEqualToRealizedValue();
    }

    private Institution resolveInstitution(Long institutionId) {
        if (institutionId == null) {
            return null;
        }

        return institutionRepository.findById(institutionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Institution not found with id: " + institutionId
                ));
    }

    private Supplier resolveSupplier(Long supplierId) {
        if (supplierId == null) {
            return null;
        }

        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Supplier not found with id: " + supplierId
                ));
    }

    private Contract resolveContract(Long contractId) {
        if (contractId == null) {
            throw new EntityNotFoundException("Contract id must not be null");
        }

        return contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Contract not found with id: " + contractId
                ));
    }
}