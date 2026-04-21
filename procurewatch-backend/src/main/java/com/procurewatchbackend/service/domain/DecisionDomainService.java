package com.procurewatchbackend.service.domain;

import com.procurewatchbackend.dto.create.CreateDecisionDto;
import com.procurewatchbackend.dto.edit.EditDecisionDto;
import com.procurewatchbackend.model.entity.Decision;

import java.time.LocalDate;
import java.util.List;

public interface DecisionDomainService {

    Decision add(CreateDecisionDto dto);

    Decision edit(Long id, EditDecisionDto dto);

    void delete(Long id);

    List<Decision> getAllForInstitution(Long institutionId);

    List<Decision> getAllForSuplier(Long supplierId);

    List<Decision> getAllByProcedureType(String procedureType);

    List<Decision> getAllBySubject(String subject);

    List<Decision> getAllByDecisionDate(LocalDate decisionDate);

    List<Decision> getAllByNotice(Long noticeId);
}