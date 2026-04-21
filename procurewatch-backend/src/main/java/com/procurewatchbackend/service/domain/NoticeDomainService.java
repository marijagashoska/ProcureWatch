package com.procurewatchbackend.service.domain;

import com.procurewatchbackend.dto.create.CreateNoticeDto;
import com.procurewatchbackend.dto.edit.EditNoticeDto;
import com.procurewatchbackend.model.entity.Notice;

import java.util.List;

public interface NoticeDomainService {

    Notice add(CreateNoticeDto dto);

    Notice edit(Long id, EditNoticeDto dto);

    void delete(Long id);

    List<Notice> allByInstitution(Long institutionId);

    List<Notice> allBySubject(String subject);

    List<Notice> allByContractType(String contractType);

    List<Notice> allByProcedureType(String procedureType);

    List<Notice> allThatHavePlanItem();

    List<Notice> allThatDontHavePlanItem();

    List<Notice> allThatHaveDecision();

    List<Notice> allThatDontHaveDecision();
}