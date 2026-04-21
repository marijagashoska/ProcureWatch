package com.procurewatchbackend.service.domain;

import com.procurewatchbackend.dto.create.CreateRealizedContractDto;
import com.procurewatchbackend.dto.edit.EditRealizedContractDto;
import com.procurewatchbackend.model.entity.RealizedContract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RealizedContractDomainService {

    RealizedContract add(CreateRealizedContractDto dto);

    RealizedContract edit(Long id, EditRealizedContractDto dto);

    void delete(Long id);

    List<RealizedContract> getAllByInstitution(Long institutionId);

    List<RealizedContract> getAllBySupplier(Long supplierId);

    RealizedContract getByContract(Long contractId);

    List<RealizedContract> getByContractType(String contractType);

    List<RealizedContract> getBySubject(String subject);

    List<RealizedContract> getByProcedureType(String procedureType);

    List<RealizedContract> getByPublicationDate(LocalDate publicationDate);

    List<RealizedContract> getByRepublishDate(LocalDate republishDate);

    List<RealizedContract> getByAwardedValueVatGreaterThanEqual(BigDecimal value);

    List<RealizedContract> getByAwardedValueVatLessThanEqual(BigDecimal value);

    List<RealizedContract> getByAwardedValueVatBetween(BigDecimal min, BigDecimal max);

    List<RealizedContract> getByRealizedValueVatGreaterThanEqual(BigDecimal value);

    List<RealizedContract> getByRealizedValueVatLessThanEqual(BigDecimal value);

    List<RealizedContract> getByRealizedValueVatBetween(BigDecimal min, BigDecimal max);

    List<RealizedContract> getByPaidValueVatGreaterThanEqual(BigDecimal value);

    List<RealizedContract> getByPaidValueVatLessThanEqual(BigDecimal value);

    List<RealizedContract> getByPaidValueVatBetween(BigDecimal min, BigDecimal max);

    List<RealizedContract> allWhereRealizedValueExceedsAwardedValue();

    List<RealizedContract> allWherePaidValueExceedsRealizedValue();

    List<RealizedContract> allWherePaidValueIsLessThanOrEqualToRealizedValue();
}