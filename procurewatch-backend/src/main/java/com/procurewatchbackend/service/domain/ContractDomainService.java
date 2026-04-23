package com.procurewatchbackend.service.domain;

import com.procurewatchbackend.dto.create.CreateContractDto;
import com.procurewatchbackend.dto.edit.EditContractDto;
import com.procurewatchbackend.model.entity.Contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.procurewatchbackend.model.entity.Institution;
import com.procurewatchbackend.model.enums.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContractDomainService {

    Contract add(CreateContractDto dto);

    Contract edit(Long id, EditContractDto dto);

    void delete(Long id);

    List<Contract> getAllByInstitution(Long institutionId);

    List<Contract> getAllBySupplier(Long supplierId);

    List<Contract> getByContractType(String contractType);

    List<Contract> getBySubject(String subject);

    List<Contract> getByProcedureType(String procedureType);

    List<Contract> getByPublicationDate(LocalDate publicationDate);

    List<Contract> getByEstimatedValueVatGreaterThanEqual(BigDecimal value);

    List<Contract> getByEstimatedValueVatLessThanEqual(BigDecimal value);

    List<Contract> getByEstimatedValueVatBetween(BigDecimal min, BigDecimal max);

    List<Contract> getByContractValueVatGreaterThanEqual(BigDecimal value);

    List<Contract> getByContractValueVatLessThanEqual(BigDecimal value);

    List<Contract> getByContractValueVatBetween(BigDecimal min, BigDecimal max);

    List<Contract> allThatHaveDecision();

    List<Contract> allThatDontHaveDecision();

    List<Contract> allThatHaveRealizedContract();

    List<Contract> allThatDontHaveRealizedContract();

    List<Contract> allWhereContractValueExceedsEstimatedValue();

    List<Contract> allWhereContractValueIsLessThanOrEqualToEstimatedValue();

    Page<Contract> search(
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
    );
    Page<Contract> getAllPaginated(Pageable pageable);

}