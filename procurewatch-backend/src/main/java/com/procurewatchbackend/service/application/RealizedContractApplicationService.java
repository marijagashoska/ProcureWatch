package com.procurewatchbackend.service.application;

import com.procurewatchbackend.dto.create.CreateRealizedContractDto;
import com.procurewatchbackend.dto.display.GetRealizedContractDto;
import com.procurewatchbackend.dto.edit.EditRealizedContractDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RealizedContractApplicationService {

    GetRealizedContractDto add(CreateRealizedContractDto dto);

    GetRealizedContractDto edit(Long id, EditRealizedContractDto dto);

    void delete(Long id);

    List<GetRealizedContractDto> getAllByInstitution(Long institutionId);

    List<GetRealizedContractDto> getAllBySupplier(Long supplierId);

    GetRealizedContractDto getByContract(Long contractId);

    List<GetRealizedContractDto> getByContractType(String contractType);

    List<GetRealizedContractDto> getBySubject(String subject);

    List<GetRealizedContractDto> getByProcedureType(String procedureType);

    List<GetRealizedContractDto> getByPublicationDate(LocalDate publicationDate);

    List<GetRealizedContractDto> getByRepublishDate(LocalDate republishDate);

    List<GetRealizedContractDto> getByAwardedValueVatGreaterThanEqual(BigDecimal value);

    List<GetRealizedContractDto> getByAwardedValueVatLessThanEqual(BigDecimal value);

    List<GetRealizedContractDto> getByAwardedValueVatBetween(BigDecimal min, BigDecimal max);

    List<GetRealizedContractDto> getByRealizedValueVatGreaterThanEqual(BigDecimal value);

    List<GetRealizedContractDto> getByRealizedValueVatLessThanEqual(BigDecimal value);

    List<GetRealizedContractDto> getByRealizedValueVatBetween(BigDecimal min, BigDecimal max);

    List<GetRealizedContractDto> getByPaidValueVatGreaterThanEqual(BigDecimal value);

    List<GetRealizedContractDto> getByPaidValueVatLessThanEqual(BigDecimal value);

    List<GetRealizedContractDto> getByPaidValueVatBetween(BigDecimal min, BigDecimal max);

    List<GetRealizedContractDto> allWhereRealizedValueExceedsAwardedValue();

    List<GetRealizedContractDto> allWherePaidValueExceedsRealizedValue();

    List<GetRealizedContractDto> allWherePaidValueIsLessThanOrEqualToRealizedValue();
}