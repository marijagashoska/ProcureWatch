package com.procurewatchbackend.repository;

import com.procurewatchbackend.model.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    List<Contract> findByInstitutionId(Long institutionId);

    List<Contract> findBySupplierId(Long supplierId);

    List<Contract> findByContractTypeIgnoreCase(String contractType);

    List<Contract> findBySubjectContainingIgnoreCase(String subject);

    List<Contract> findByProcedureTypeIgnoreCase(String procedureType);

    List<Contract> findByPublicationDate(LocalDate publicationDate);

    List<Contract> findByEstimatedValueVatGreaterThanEqual(BigDecimal value);

    List<Contract> findByEstimatedValueVatLessThanEqual(BigDecimal value);

    List<Contract> findByEstimatedValueVatBetween(BigDecimal min, BigDecimal max);

    List<Contract> findByContractValueVatGreaterThanEqual(BigDecimal value);

    List<Contract> findByContractValueVatLessThanEqual(BigDecimal value);

    List<Contract> findByContractValueVatBetween(BigDecimal min, BigDecimal max);

    @Query("select c from Contract c where c.decision is not null")
    List<Contract> findAllThatHaveDecision();

    @Query("select c from Contract c where c.decision is null")
    List<Contract> findAllThatDontHaveDecision();

    @Query("select c from Contract c where c.realizedContract is not null")
    List<Contract> findAllThatHaveRealizedContract();

    @Query("select c from Contract c where c.realizedContract is null")
    List<Contract> findAllThatDontHaveRealizedContract();

    @Query("""
           select c from Contract c
           where c.contractValueVat is not null
             and c.estimatedValueVat is not null
             and c.contractValueVat > c.estimatedValueVat
           """)
    List<Contract> findAllWhereContractValueExceedsEstimatedValue();

    @Query("""
           select c from Contract c
           where c.contractValueVat is not null
             and c.estimatedValueVat is not null
             and c.contractValueVat <= c.estimatedValueVat
           """)
    List<Contract> findAllWhereContractValueIsLessThanOrEqualToEstimatedValue();
}