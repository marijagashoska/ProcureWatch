package com.procurewatchbackend.service.application.impl;

import com.procurewatchbackend.dto.create.CreateContractDto;
import com.procurewatchbackend.dto.display.GetContractDto;
import com.procurewatchbackend.dto.display.GetInstitutionDto;
import com.procurewatchbackend.dto.edit.EditContractDto;
import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.service.application.ContractApplicationService;
import com.procurewatchbackend.service.domain.ContractDomainService;
import com.procurewatchbackend.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.procurewatchbackend.dto.display.GetContractTableRowDto;
import com.procurewatchbackend.dto.display.PagedResponseDto;
import com.procurewatchbackend.model.enums.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContractApplicationServiceImpl implements ContractApplicationService {

    private final ContractDomainService contractDomainService;

    @Override
    @Transactional
    public GetContractDto add(CreateContractDto dto) {
        return mapToGetDto(contractDomainService.add(dto));
    }

    @Override
    @Transactional
    public GetContractDto edit(Long id, EditContractDto dto) {
        return mapToGetDto(contractDomainService.edit(id, dto));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        contractDomainService.delete(id);
    }

    @Override
    public List<GetContractDto> getAllByInstitution(Long institutionId) {
        return contractDomainService.getAllByInstitution(institutionId)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetContractDto> getAllBySupplier(Long supplierId) {
        return contractDomainService.getAllBySupplier(supplierId)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetContractDto> getByContractType(String contractType) {
        return contractDomainService.getByContractType(contractType)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetContractDto> getBySubject(String subject) {
        return contractDomainService.getBySubject(subject)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetContractDto> getByProcedureType(String procedureType) {
        return contractDomainService.getByProcedureType(procedureType)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetContractDto> getByPublicationDate(LocalDate publicationDate) {
        return contractDomainService.getByPublicationDate(publicationDate)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetContractDto> getByEstimatedValueVatGreaterThanEqual(BigDecimal value) {
        return contractDomainService.getByEstimatedValueVatGreaterThanEqual(value)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetContractDto> getByEstimatedValueVatLessThanEqual(BigDecimal value) {
        return contractDomainService.getByEstimatedValueVatLessThanEqual(value)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetContractDto> getByEstimatedValueVatBetween(BigDecimal min, BigDecimal max) {
        return contractDomainService.getByEstimatedValueVatBetween(min, max)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetContractDto> getByContractValueVatGreaterThanEqual(BigDecimal value) {
        return contractDomainService.getByContractValueVatGreaterThanEqual(value)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetContractDto> getByContractValueVatLessThanEqual(BigDecimal value) {
        return contractDomainService.getByContractValueVatLessThanEqual(value)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetContractDto> getByContractValueVatBetween(BigDecimal min, BigDecimal max) {
        return contractDomainService.getByContractValueVatBetween(min, max)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetContractDto> allThatHaveDecision() {
        return contractDomainService.allThatHaveDecision()
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetContractDto> allThatDontHaveDecision() {
        return contractDomainService.allThatDontHaveDecision()
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetContractDto> allThatHaveRealizedContract() {
        return contractDomainService.allThatHaveRealizedContract()
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetContractDto> allThatDontHaveRealizedContract() {
        return contractDomainService.allThatDontHaveRealizedContract()
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetContractDto> allWhereContractValueExceedsEstimatedValue() {
        return contractDomainService.allWhereContractValueExceedsEstimatedValue()
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetContractDto> allWhereContractValueIsLessThanOrEqualToEstimatedValue() {
        return contractDomainService.allWhereContractValueIsLessThanOrEqualToEstimatedValue()
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    private GetContractDto mapToGetDto(Contract contract) {
        return new GetContractDto(
                contract.getId(),
                contract.getInstitution().getId(),
                contract.getSupplier() != null ? contract.getSupplier().getId() : null,
                contract.getDecision() != null ? contract.getDecision().getId() : null,
                contract.getRealizedContract() != null ? contract.getRealizedContract().getId() : null,
                contract.getNoticeNumber(),
                contract.getSubject(),
                contract.getContractType(),
                contract.getProcedureType(),
                contract.getContractDate(),
                contract.getPublicationDate(),
                contract.getEstimatedValueVat(),
                contract.getContractValueVat(),
                contract.getCurrency(),
                contract.getSourceUrl()
        );
    }

    @Override
    public PagedResponseDto<GetContractTableRowDto> search(
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
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(resolveDirection(sortDir), resolveSortProperty(sortBy))
        );

        Page<Contract> contractsPage = contractDomainService.search(
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
                riskLevel,
                pageable
        );

        return new PagedResponseDto<>(
                contractsPage.getContent().stream()
                        .map(this::mapToTableRowDto)
                        .toList(),
                contractsPage.getNumber(),
                contractsPage.getSize(),
                contractsPage.getTotalElements(),
                contractsPage.getTotalPages(),
                contractsPage.isFirst(),
                contractsPage.isLast(),
                sortBy,
                sortDir
        );
    }

    private GetContractTableRowDto mapToTableRowDto(Contract contract) {
        return new GetContractTableRowDto(
                contract.getId(),
                contract.getNoticeNumber(),
                contract.getInstitution() != null ? contract.getInstitution().getOfficialName() : null,
                contract.getSupplier() != null ? contract.getSupplier().getOfficialName() : null,
                contract.getSubject(),
                contract.getContractType(),
                contract.getProcedureType(),
                contract.getContractValueVat(),
                contract.getContractDate(),
                contract.getRiskAssessment() != null ? contract.getRiskAssessment().getFinalRiskScore() : null,
                contract.getRiskAssessment() != null ? contract.getRiskAssessment().getRiskLevel() : null
        );
    }

    private Sort.Direction resolveDirection(String sortDir) {
        return "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private String resolveSortProperty(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "contractDate";
        }

        return switch (sortBy) {
            case "noticeNumber" -> "noticeNumber";
            case "institutionName" -> "institution.officialName";
            case "supplierName" -> "supplier.officialName";
            case "subject" -> "subject";
            case "value" -> "contractValueVat";
            case "contractDate" -> "contractDate";
            case "riskScore" -> "riskAssessment.finalRiskScore";
            case "riskLevel" -> "riskAssessment.riskLevel";
            default -> "contractDate";
        };
    }
    @Override
    public PagedResponseDto<GetContractDto> getAllPaginated(
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Pageable pageable = PageUtils.createPageable(page, size, sortBy, sortDir);

        Page<GetContractDto> result = contractDomainService.getAllPaginated(pageable)
                .map(this::mapToGetDto);

        return PageUtils.toPagedResponse(result, sortBy, sortDir);
    }
}