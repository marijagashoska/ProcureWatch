package com.procurewatchbackend.service.application;

import com.procurewatchbackend.dto.create.CreateContractDto;
import com.procurewatchbackend.dto.display.GetContractDto;
import com.procurewatchbackend.dto.display.GetContractTableRowDto;
import com.procurewatchbackend.dto.display.PagedResponseDto;
import com.procurewatchbackend.dto.edit.EditContractDto;
import com.procurewatchbackend.model.enums.RiskLevel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ContractApplicationService {
    GetContractDto add(CreateContractDto dto);
    GetContractDto edit(Long id, EditContractDto dto);
    void delete(Long id);
    List<GetContractDto> getAllByInstitution(Long institutionId);
    List<GetContractDto> getAllBySupplier(Long supplierId);
    List<GetContractDto> getByContractType(String contractType);
    List<GetContractDto> getBySubject(String subject);
    List<GetContractDto> getByProcedureType(String procedureType);
    List<GetContractDto> getByPublicationDate(LocalDate publicationDate);
    List<GetContractDto> getByEstimatedValueVatGreaterThanEqual(BigDecimal value);
    List<GetContractDto> getByEstimatedValueVatLessThanEqual(BigDecimal value);
    List<GetContractDto> getByEstimatedValueVatBetween(BigDecimal min, BigDecimal max);
    List<GetContractDto> getByContractValueVatGreaterThanEqual(BigDecimal value);
    List<GetContractDto> getByContractValueVatLessThanEqual(BigDecimal value);
    List<GetContractDto> getByContractValueVatBetween(BigDecimal min, BigDecimal max);
    List<GetContractDto> allThatHaveDecision();
    List<GetContractDto> allThatDontHaveDecision();
    List<GetContractDto> allThatHaveRealizedContract();
    List<GetContractDto> allThatDontHaveRealizedContract();
    List<GetContractDto> allWhereContractValueExceedsEstimatedValue();
    List<GetContractDto> allWhereContractValueIsLessThanOrEqualToEstimatedValue();
    PagedResponseDto<GetContractTableRowDto> search(
            String searchText, String noticeNumber, Long institutionId, Long supplierId,
            String contractType, String procedureType, LocalDate dateFrom, LocalDate dateTo,
            BigDecimal minValue, BigDecimal maxValue, RiskLevel riskLevel,
            int page, int size, String sortBy, String sortDir);
    PagedResponseDto<GetContractDto> getAllPaginated(int page, int size, String sortBy, String sortDir);
}