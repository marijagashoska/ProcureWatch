package com.procurewatchbackend.service.application.impl;

import com.procurewatchbackend.dto.create.CreateNoticeDto;
import com.procurewatchbackend.dto.display.GetDecisionDto;
import com.procurewatchbackend.dto.display.GetNoticeDto;
import com.procurewatchbackend.dto.display.PagedResponseDto;
import com.procurewatchbackend.dto.edit.EditNoticeDto;
import com.procurewatchbackend.model.entity.Decision;
import com.procurewatchbackend.model.entity.Notice;
import com.procurewatchbackend.service.application.NoticeApplicationService;
import com.procurewatchbackend.service.domain.NoticeDomainService;
import com.procurewatchbackend.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeApplicationServiceImpl implements NoticeApplicationService {

    private final NoticeDomainService noticeDomainService;

    @Override
    @Transactional
    public GetNoticeDto add(CreateNoticeDto dto) {
        return mapToGetDto(noticeDomainService.add(dto));
    }

    @Override
    @Transactional
    public GetNoticeDto edit(Long id, EditNoticeDto dto) {
        return mapToGetDto(noticeDomainService.edit(id, dto));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        noticeDomainService.delete(id);
    }

    @Override
    public List<GetNoticeDto> allByInstitution(Long institutionId) {
        return noticeDomainService.allByInstitution(institutionId)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetNoticeDto> allBySubject(String subject) {
        return noticeDomainService.allBySubject(subject)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetNoticeDto> allByContractType(String contractType) {
        return noticeDomainService.allByContractType(contractType)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetNoticeDto> allByProcedureType(String procedureType) {
        return noticeDomainService.allByProcedureType(procedureType)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetNoticeDto> allThatHavePlanItem() {
        return noticeDomainService.allThatHavePlanItem()
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetNoticeDto> allThatDontHavePlanItem() {
        return noticeDomainService.allThatDontHavePlanItem()
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetNoticeDto> allThatHaveDecision() {
        return noticeDomainService.allThatHaveDecision()
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetNoticeDto> allThatDontHaveDecision() {
        return noticeDomainService.allThatDontHaveDecision()
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    private GetNoticeDto mapToGetDto(Notice notice) {
        List<Long> decisionIds = notice.getDecisions() == null
                ? Collections.emptyList()
                : notice.getDecisions().stream().map(Decision::getId).toList();

        return new GetNoticeDto(
                notice.getId(),
                notice.getInstitution().getId(),
                notice.getPlanItem() != null ? notice.getPlanItem().getId() : null,
                decisionIds,
                notice.getNoticeNumber(),
                notice.getSubject(),
                notice.getContractType(),
                notice.getProcedureType(),
                notice.getPublicationDate(),
                notice.getDeadlineDate(),
                notice.getSourceUrl()
        );
    }
    @Override
    public PagedResponseDto<GetNoticeDto> getAllPaginated(
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Pageable pageable = PageUtils.createPageable(page, size, sortBy, sortDir);

        Page<GetNoticeDto> result = noticeDomainService.getAllPaginated(pageable)
                .map(this::mapToGetDto);

        return PageUtils.toPagedResponse(result, sortBy, sortDir);
    }
}