package com.procurewatchbackend.repository;

import com.procurewatchbackend.model.entity.Decision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DecisionRepository extends JpaRepository<Decision, Long> {

    List<Decision> findByInstitutionId(Long institutionId);

    List<Decision> findBySupplierId(Long supplierId);

    List<Decision> findByProcedureTypeIgnoreCase(String procedureType);

    List<Decision> findBySubjectContainingIgnoreCase(String subject);

    List<Decision> findByDecisionDate(LocalDate decisionDate);

    List<Decision> findByNoticeId(Long noticeId);

    void deleteByNoticeId(Long noticeId);

    Optional<Decision> findFirstByNoticeNumber(String noticeNumber);

    Optional<Decision> findFirstByNoticeNumberIgnoreCase(String noticeNumber);

    Optional<Decision> findFirstByNoticeNumberAndSupplierIdAndDecisionDate(
            String noticeNumber,
            Long supplierId,
            LocalDate decisionDate
    );
    List<Decision> findByNoticeNumberOrderByIdAsc(String noticeNumber);

    Optional<Decision> findFirstByNoticeNumberOrderByIdAsc(String noticeNumber);
    Optional<Decision> findFirstBySourceUrl(String sourceUrl);

    List<Decision> findByNoticeNumberIgnoreCaseOrderByIdAsc(String noticeNumber);

    Optional<Decision> findFirstByNoticeNumberAndSubjectContainingIgnoreCaseAndDecisionTextContainingIgnoreCase(
            String noticeNumber,
            String subject,
            String decisionText
    );

    Optional<Decision> findFirstByNoticeNumberIgnoreCaseAndSupplierIdOrderByIdAsc(
            String noticeNumber,
            Long supplierId
    );

    List<Decision> findByContractId(Long contractId);
}