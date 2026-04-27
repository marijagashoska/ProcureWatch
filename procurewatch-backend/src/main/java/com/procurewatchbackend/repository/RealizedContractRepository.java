package com.procurewatchbackend.repository;

import com.procurewatchbackend.model.entity.RealizedContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RealizedContractRepository extends JpaRepository<RealizedContract, Long> {

    List<RealizedContract> findByInstitutionId(Long institutionId);

    List<RealizedContract> findBySupplierId(Long supplierId);

    Optional<RealizedContract> findByContractId(Long contractId);

    List<RealizedContract> findByContractTypeIgnoreCase(String contractType);

    List<RealizedContract> findBySubjectContainingIgnoreCase(String subject);

    List<RealizedContract> findByProcedureTypeIgnoreCase(String procedureType);

    List<RealizedContract> findByPublicationDate(LocalDate publicationDate);

    List<RealizedContract> findByRepublishDate(LocalDate republishDate);

    List<RealizedContract> findByAwardedValueVatGreaterThanEqual(BigDecimal value);

    List<RealizedContract> findByAwardedValueVatLessThanEqual(BigDecimal value);

    List<RealizedContract> findByAwardedValueVatBetween(BigDecimal min, BigDecimal max);

    List<RealizedContract> findByRealizedValueVatGreaterThanEqual(BigDecimal value);

    List<RealizedContract> findByRealizedValueVatLessThanEqual(BigDecimal value);

    List<RealizedContract> findByRealizedValueVatBetween(BigDecimal min, BigDecimal max);

    List<RealizedContract> findByPaidValueVatGreaterThanEqual(BigDecimal value);

    List<RealizedContract> findByPaidValueVatLessThanEqual(BigDecimal value);

    List<RealizedContract> findByPaidValueVatBetween(BigDecimal min, BigDecimal max);

    @Query("""
           select rc from RealizedContract rc
           where rc.realizedValueVat is not null
             and rc.awardedValueVat is not null
             and rc.realizedValueVat > rc.awardedValueVat
           """)
    List<RealizedContract> findAllWhereRealizedValueExceedsAwardedValue();

    @Query("""
           select rc from RealizedContract rc
           where rc.paidValueVat is not null
             and rc.realizedValueVat is not null
             and rc.paidValueVat > rc.realizedValueVat
           """)
    List<RealizedContract> findAllWherePaidValueExceedsRealizedValue();

    @Query("""
           select rc from RealizedContract rc
           where rc.paidValueVat is not null
             and rc.realizedValueVat is not null
             and rc.paidValueVat <= rc.realizedValueVat
           """)
    List<RealizedContract> findAllWherePaidValueIsLessThanOrEqualToRealizedValue();

    Optional<RealizedContract> findFirstByNoticeNumber(String noticeNumber);

    Optional<RealizedContract> findFirstByNoticeNumberIgnoreCase(String noticeNumber);

    Optional<RealizedContract> findFirstByNoticeNumberAndInstitutionIdAndSupplierId(
            String noticeNumber,
            Long institutionId,
            Long supplierId
    );
}