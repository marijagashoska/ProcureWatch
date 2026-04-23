package com.procurewatchbackend.service.application.impl;

import com.procurewatchbackend.dto.create.CreateRealizedContractDto;
import com.procurewatchbackend.dto.display.GetProcurementPlanDto;
import com.procurewatchbackend.dto.display.GetRealizedContractDto;
import com.procurewatchbackend.dto.display.PagedResponseDto;
import com.procurewatchbackend.dto.edit.EditRealizedContractDto;
import com.procurewatchbackend.model.entity.RealizedContract;
import com.procurewatchbackend.service.application.RealizedContractApplicationService;
import com.procurewatchbackend.service.domain.RealizedContractDomainService;
import com.procurewatchbackend.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RealizedContractApplicationServiceImpl implements RealizedContractApplicationService {

    private final RealizedContractDomainService realizedContractDomainService;

    @Override
    @Transactional
    public GetRealizedContractDto add(CreateRealizedContractDto dto) {
        return mapToGetDto(realizedContractDomainService.add(dto));
    }

    @Override
    @Transactional
    public GetRealizedContractDto edit(Long id, EditRealizedContractDto dto) {
        return mapToGetDto(realizedContractDomainService.edit(id, dto));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        realizedContractDomainService.delete(id);
    }

    @Override
    public List<GetRealizedContractDto> getAllByInstitution(Long institutionId) {
        return realizedContractDomainService.getAllByInstitution(institutionId)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetRealizedContractDto> getAllBySupplier(Long supplierId) {
        return realizedContractDomainService.getAllBySupplier(supplierId)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public GetRealizedContractDto getByContract(Long contractId) {
        return mapToGetDto(realizedContractDomainService.getByContract(contractId));
    }

    @Override
    public List<GetRealizedContractDto> getByContractType(String contractType) {
        return realizedContractDomainService.getByContractType(contractType)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetRealizedContractDto> getBySubject(String subject) {
        return realizedContractDomainService.getBySubject(subject)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetRealizedContractDto> getByProcedureType(String procedureType) {
        return realizedContractDomainService.getByProcedureType(procedureType)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetRealizedContractDto> getByPublicationDate(LocalDate publicationDate) {
        return realizedContractDomainService.getByPublicationDate(publicationDate)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetRealizedContractDto> getByRepublishDate(LocalDate republishDate) {
        return realizedContractDomainService.getByRepublishDate(republishDate)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetRealizedContractDto> getByAwardedValueVatGreaterThanEqual(BigDecimal value) {
        return realizedContractDomainService.getByAwardedValueVatGreaterThanEqual(value)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetRealizedContractDto> getByAwardedValueVatLessThanEqual(BigDecimal value) {
        return realizedContractDomainService.getByAwardedValueVatLessThanEqual(value)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetRealizedContractDto> getByAwardedValueVatBetween(BigDecimal min, BigDecimal max) {
        return realizedContractDomainService.getByAwardedValueVatBetween(min, max)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetRealizedContractDto> getByRealizedValueVatGreaterThanEqual(BigDecimal value) {
        return realizedContractDomainService.getByRealizedValueVatGreaterThanEqual(value)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetRealizedContractDto> getByRealizedValueVatLessThanEqual(BigDecimal value) {
        return realizedContractDomainService.getByRealizedValueVatLessThanEqual(value)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetRealizedContractDto> getByRealizedValueVatBetween(BigDecimal min, BigDecimal max) {
        return realizedContractDomainService.getByRealizedValueVatBetween(min, max)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetRealizedContractDto> getByPaidValueVatGreaterThanEqual(BigDecimal value) {
        return realizedContractDomainService.getByPaidValueVatGreaterThanEqual(value)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetRealizedContractDto> getByPaidValueVatLessThanEqual(BigDecimal value) {
        return realizedContractDomainService.getByPaidValueVatLessThanEqual(value)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetRealizedContractDto> getByPaidValueVatBetween(BigDecimal min, BigDecimal max) {
        return realizedContractDomainService.getByPaidValueVatBetween(min, max)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetRealizedContractDto> allWhereRealizedValueExceedsAwardedValue() {
        return realizedContractDomainService.allWhereRealizedValueExceedsAwardedValue()
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetRealizedContractDto> allWherePaidValueExceedsRealizedValue() {
        return realizedContractDomainService.allWherePaidValueExceedsRealizedValue()
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetRealizedContractDto> allWherePaidValueIsLessThanOrEqualToRealizedValue() {
        return realizedContractDomainService.allWherePaidValueIsLessThanOrEqualToRealizedValue()
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    private GetRealizedContractDto mapToGetDto(RealizedContract realizedContract) {
        return new GetRealizedContractDto(
                realizedContract.getId(),
                realizedContract.getInstitution() != null ? realizedContract.getInstitution().getId() : null,
                realizedContract.getSupplier() != null ? realizedContract.getSupplier().getId() : null,
                realizedContract.getContract() != null ? realizedContract.getContract().getId() : null,
                realizedContract.getNoticeNumber(),
                realizedContract.getSubject(),
                realizedContract.getContractType(),
                realizedContract.getProcedureType(),
                realizedContract.getAwardedValueVat(),
                realizedContract.getRealizedValueVat(),
                realizedContract.getPaidValueVat(),
                realizedContract.getPublicationDate(),
                realizedContract.getRepublishDate(),
                realizedContract.getSourceUrl()
        );
    }

    @Override
    public PagedResponseDto<GetRealizedContractDto> getAllPaginated(
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Pageable pageable = PageUtils.createPageable(page, size, sortBy, sortDir);

        Page<GetRealizedContractDto> result = realizedContractDomainService.getAllPaginated(pageable)
                .map(this::mapToGetDto);

        return PageUtils.toPagedResponse(result, sortBy, sortDir);
    }
}