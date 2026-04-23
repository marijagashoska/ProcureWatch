package com.procurewatchbackend.service.application.impl;

import com.procurewatchbackend.dto.create.CreateDecisionDto;
import com.procurewatchbackend.dto.display.GetDecisionDto;
import com.procurewatchbackend.dto.display.GetInstitutionDto;
import com.procurewatchbackend.dto.display.PagedResponseDto;
import com.procurewatchbackend.dto.edit.EditDecisionDto;
import com.procurewatchbackend.model.entity.Decision;
import com.procurewatchbackend.service.application.DecisionApplicationService;
import com.procurewatchbackend.service.domain.DecisionDomainService;
import com.procurewatchbackend.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DecisionApplicationServiceImpl implements DecisionApplicationService {

    private final DecisionDomainService decisionDomainService;

    @Override
    @Transactional
    public GetDecisionDto add(CreateDecisionDto dto) {
        return mapToGetDto(decisionDomainService.add(dto));
    }

    @Override
    @Transactional
    public GetDecisionDto edit(Long id, EditDecisionDto dto) {
        return mapToGetDto(decisionDomainService.edit(id, dto));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        decisionDomainService.delete(id);
    }

    @Override
    public List<GetDecisionDto> getAllForInstitution(Long institutionId) {
        return decisionDomainService.getAllForInstitution(institutionId)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetDecisionDto> getAllForSuplier(Long supplierId) {
        return decisionDomainService.getAllForSuplier(supplierId)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetDecisionDto> getAllByProcedureType(String procedureType) {
        return decisionDomainService.getAllByProcedureType(procedureType)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetDecisionDto> getAllBySubject(String subject) {
        return decisionDomainService.getAllBySubject(subject)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public List<GetDecisionDto> getAllByDecisionDate(LocalDate decisionDate) {
        return decisionDomainService.getAllByDecisionDate(decisionDate)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    private GetDecisionDto mapToGetDto(Decision decision) {
        return new GetDecisionDto(
                decision.getId(),
                decision.getNotice().getId(),
                decision.getContract().getId(),
                decision.getInstitution().getId(),
                decision.getSupplier() != null ? decision.getSupplier().getId() : null,
                decision.getNoticeNumber(),
                decision.getDecisionDate(),
                decision.getSubject(),
                decision.getDecisionText(),
                decision.getProcedureType(),
                decision.getSourceUrl()
        );
    }

    @Override
    public List<GetDecisionDto> getAllByNotice(Long noticeId) {
        return decisionDomainService.getAllByNotice(noticeId)
                .stream()
                .map(this::mapToGetDto)
                .toList();
    }

    @Override
    public PagedResponseDto<GetDecisionDto> getAllPaginated(
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Pageable pageable = PageUtils.createPageable(page, size, sortBy, sortDir);

        Page<GetDecisionDto> result = decisionDomainService.getAllPaginated(pageable)
                .map(this::mapToGetDto);

        return PageUtils.toPagedResponse(result, sortBy, sortDir);
    }
}