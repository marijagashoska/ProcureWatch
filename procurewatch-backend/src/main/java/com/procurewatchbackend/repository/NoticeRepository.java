package com.procurewatchbackend.repository;

import com.procurewatchbackend.model.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findByInstitutionId(Long institutionId);

    List<Notice> findBySubjectContainingIgnoreCase(String subject);

    List<Notice> findByContractTypeIgnoreCase(String contractType);

    List<Notice> findByProcedureTypeIgnoreCase(String procedureType);

    List<Notice> findByPlanItemIsNotNull();

    List<Notice> findByPlanItemIsNull();

    @Query("select distinct n from Notice n where n.decisions is not empty")
    List<Notice> findAllThatHaveDecision();

    @Query("select distinct n from Notice n where n.decisions is empty")
    List<Notice> findAllThatDontHaveDecision();
}