package com.procurewatchbackend.service.application;

import com.procurewatchbackend.dto.create.CreateDecisionDto;
import com.procurewatchbackend.dto.display.GetDecisionDto;
import com.procurewatchbackend.dto.edit.EditDecisionDto;

import java.time.LocalDate;
import java.util.List;

public interface DecisionApplicationService {

    GetDecisionDto add(CreateDecisionDto dto);

    GetDecisionDto edit(Long id, EditDecisionDto dto);

    void delete(Long id);

    List<GetDecisionDto> getAllForInstitution(Long institutionId);

    List<GetDecisionDto> getAllForSuplier(Long supplierId);

    List<GetDecisionDto> getAllByProcedureType(String procedureType);

    List<GetDecisionDto> getAllBySubject(String subject);

    List<GetDecisionDto> getAllByDecisionDate(LocalDate decisionDate);

    List<GetDecisionDto> getAllByNotice(Long noticeId);
}