package com.procurewatchbackend.service.domain.impl;

import com.procurewatchbackend.dto.create.CreateContractDto;
import com.procurewatchbackend.dto.edit.EditContractDto;
import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.model.entity.Institution;
import com.procurewatchbackend.model.entity.Supplier;
import com.procurewatchbackend.repository.ContractRepository;
import com.procurewatchbackend.repository.InstitutionRepository;
import com.procurewatchbackend.repository.SupplierRepository;
import com.procurewatchbackend.service.domain.ContractDomainService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.procurewatchbackend.model.enums.RiskLevel;
import com.procurewatchbackend.repository.specification.ContractSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ContractDomainServiceImpl implements ContractDomainService {

    private final ContractRepository contractRepository;
    private final InstitutionRepository institutionRepository;
    private final SupplierRepository supplierRepository;

    @Override
    public Contract add(CreateContractDto dto) {
        Institution institution = institutionRepository.findById(dto.institutionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Institution not found with id: " + dto.institutionId()
                ));

        Supplier supplier = resolveSupplier(dto.supplierId());

        Contract contract = new Contract();
        contract.setInstitution(institution);
        contract.setSupplier(supplier);
        contract.setNoticeNumber(dto.noticeNumber());
        contract.setSubject(dto.subject());
        contract.setContractType(dto.contractType());
        contract.setProcedureType(dto.procedureType());
        contract.setContractDate(dto.contractDate());
        contract.setPublicationDate(dto.publicationDate());
        contract.setEstimatedValueVat(dto.estimatedValueVat());
        contract.setContractValueVat(dto.contractValueVat());
        contract.setCurrency(dto.currency());
        contract.setSourceUrl(dto.sourceUrl());

        return contractRepository.save(contract);
    }

    @Override
    public Contract edit(Long id, EditContractDto dto) {
        Contract existing = contractRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Contract not found with id: " + id
                ));

        Institution institution = institutionRepository.findById(dto.institutionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Institution not found with id: " + dto.institutionId()
                ));

        Supplier supplier = resolveSupplier(dto.supplierId());

        existing.setInstitution(institution);
        existing.setSupplier(supplier);
        existing.setNoticeNumber(dto.noticeNumber());
        existing.setSubject(dto.subject());
        existing.setContractType(dto.contractType());
        existing.setProcedureType(dto.procedureType());
        existing.setContractDate(dto.contractDate());
        existing.setPublicationDate(dto.publicationDate());
        existing.setEstimatedValueVat(dto.estimatedValueVat());
        existing.setContractValueVat(dto.contractValueVat());
        existing.setCurrency(dto.currency());
        existing.setSourceUrl(dto.sourceUrl());

        return contractRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Contract existing = contractRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Contract not found with id: " + id
                ));

        contractRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> getAllByInstitution(Long institutionId) {
        return contractRepository.findByInstitutionId(institutionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> getAllBySupplier(Long supplierId) {
        return contractRepository.findBySupplierId(supplierId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> getByContractType(String contractType) {
        return contractRepository.findByContractTypeIgnoreCase(contractType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> getBySubject(String subject) {
        return contractRepository.findBySubjectContainingIgnoreCase(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> getByProcedureType(String procedureType) {
        return contractRepository.findByProcedureTypeIgnoreCase(procedureType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> getByPublicationDate(LocalDate publicationDate) {
        return contractRepository.findByPublicationDate(publicationDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> getByEstimatedValueVatGreaterThanEqual(BigDecimal value) {
        return contractRepository.findByEstimatedValueVatGreaterThanEqual(value);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> getByEstimatedValueVatLessThanEqual(BigDecimal value) {
        return contractRepository.findByEstimatedValueVatLessThanEqual(value);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> getByEstimatedValueVatBetween(BigDecimal min, BigDecimal max) {
        return contractRepository.findByEstimatedValueVatBetween(min, max);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> getByContractValueVatGreaterThanEqual(BigDecimal value) {
        return contractRepository.findByContractValueVatGreaterThanEqual(value);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> getByContractValueVatLessThanEqual(BigDecimal value) {
        return contractRepository.findByContractValueVatLessThanEqual(value);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> getByContractValueVatBetween(BigDecimal min, BigDecimal max) {
        return contractRepository.findByContractValueVatBetween(min, max);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> allThatHaveDecision() {
        return contractRepository.findAllThatHaveDecision();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> allThatDontHaveDecision() {
        return contractRepository.findAllThatDontHaveDecision();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> allThatHaveRealizedContract() {
        return contractRepository.findAllThatHaveRealizedContract();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> allThatDontHaveRealizedContract() {
        return contractRepository.findAllThatDontHaveRealizedContract();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> allWhereContractValueExceedsEstimatedValue() {
        return contractRepository.findAllWhereContractValueExceedsEstimatedValue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> allWhereContractValueIsLessThanOrEqualToEstimatedValue() {
        return contractRepository.findAllWhereContractValueIsLessThanOrEqualToEstimatedValue();
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

    @Override
    @Transactional(readOnly = true)
    public Page<Contract> search(
            String searchText,
            String noticeNumber,
            Long institutionId,
            Long supplierId,
            String contractType,
            String procedureType,
            LocalDate dateFrom,
            LocalDate dateTo,
            BigDecimal minValue,
            BigDecimal maxValue,
            RiskLevel riskLevel,
            Pageable pageable
    ) {
        Specification<Contract> specification = ContractSpecifications.byFilters(
                searchText,
                noticeNumber,
                institutionId,
                supplierId,
                contractType,
                procedureType,
                dateFrom,
                dateTo,
                minValue,
                maxValue,
                riskLevel
        );

        return contractRepository.findAll(specification, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Contract> getAllPaginated(Pageable pageable) {
        return contractRepository.findAll(pageable);
    }
}