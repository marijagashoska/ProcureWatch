package com.procurewatchbackend.service.application;

import com.procurewatchbackend.dto.create.CreateNoticeDto;
import com.procurewatchbackend.dto.display.GetInstitutionDto;
import com.procurewatchbackend.dto.display.GetNoticeDto;
import com.procurewatchbackend.dto.display.PagedResponseDto;
import com.procurewatchbackend.dto.edit.EditNoticeDto;

import java.util.List;

public interface NoticeApplicationService {

    GetNoticeDto add(CreateNoticeDto dto);

    GetNoticeDto edit(Long id, EditNoticeDto dto);

    void delete(Long id);

    List<GetNoticeDto> allByInstitution(Long institutionId);

    List<GetNoticeDto> allBySubject(String subject);

    List<GetNoticeDto> allByContractType(String contractType);

    List<GetNoticeDto> allByProcedureType(String procedureType);

    List<GetNoticeDto> allThatHavePlanItem();

    List<GetNoticeDto> allThatDontHavePlanItem();

    List<GetNoticeDto> allThatHaveDecision();

    List<GetNoticeDto> allThatDontHaveDecision();

    PagedResponseDto<GetNoticeDto> getAllPaginated(
            int page,
            int size,
            String sortBy,
            String sortDir
    );
}