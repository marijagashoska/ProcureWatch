package com.procurewatchbackend.service.application.impl;

import com.procurewatchbackend.dto.create.CreateContractDto;
import com.procurewatchbackend.dto.display.GetContractDto;
import com.procurewatchbackend.dto.edit.EditContractDto;
import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.service.application.ContractApplicationService;
import com.procurewatchbackend.service.domain.ContractDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}