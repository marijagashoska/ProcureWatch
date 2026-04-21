package com.procurewatchbackend.service.domain.impl;

import com.procurewatchbackend.dto.create.CreateNoticeDto;
import com.procurewatchbackend.dto.edit.EditNoticeDto;
import com.procurewatchbackend.model.entity.Institution;
import com.procurewatchbackend.model.entity.Notice;
import com.procurewatchbackend.model.entity.PlanItem;
import com.procurewatchbackend.repository.DecisionRepository;
import com.procurewatchbackend.repository.InstitutionRepository;
import com.procurewatchbackend.repository.NoticeRepository;
import com.procurewatchbackend.repository.PlanItemRepository;
import com.procurewatchbackend.service.domain.NoticeDomainService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeDomainServiceImpl implements NoticeDomainService {

    private final NoticeRepository noticeRepository;
    private final InstitutionRepository institutionRepository;
    private final PlanItemRepository planItemRepository;
    private final DecisionRepository decisionRepository;

    @Override
    public Notice add(CreateNoticeDto dto) {
        Institution institution = institutionRepository.findById(dto.institutionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Institution not found with id: " + dto.institutionId()
                ));

        PlanItem planItem = resolvePlanItem(dto.planItemId());

        Notice notice = new Notice();
        notice.setInstitution(institution);
        notice.setPlanItem(planItem);
        notice.setNoticeNumber(dto.noticeNumber());
        notice.setSubject(dto.subject());
        notice.setContractType(dto.contractType());
        notice.setProcedureType(dto.procedureType());
        notice.setPublicationDate(dto.publicationDate());
        notice.setDeadlineDate(dto.deadlineDate());
        notice.setSourceUrl(dto.sourceUrl());

        Notice saved = noticeRepository.save(notice);
        attachDecisions(saved);

        return saved;
    }

    @Override
    public Notice edit(Long id, EditNoticeDto dto) {
        Notice existing = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Notice not found with id: " + id
                ));

        Institution institution = institutionRepository.findById(dto.institutionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Institution not found with id: " + dto.institutionId()
                ));

        PlanItem planItem = resolvePlanItem(dto.planItemId());

        existing.setInstitution(institution);
        existing.setPlanItem(planItem);
        existing.setNoticeNumber(dto.noticeNumber());
        existing.setSubject(dto.subject());
        existing.setContractType(dto.contractType());
        existing.setProcedureType(dto.procedureType());
        existing.setPublicationDate(dto.publicationDate());
        existing.setDeadlineDate(dto.deadlineDate());
        existing.setSourceUrl(dto.sourceUrl());

        Notice updated = noticeRepository.save(existing);
        attachDecisions(updated);

        return updated;
    }

    @Override
    public void delete(Long id) {
        Notice existing = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Notice not found with id: " + id
                ));

        decisionRepository.deleteByNoticeId(existing.getId());
        noticeRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notice> allByInstitution(Long institutionId) {
        List<Notice> notices = noticeRepository.findByInstitutionId(institutionId);
        notices.forEach(this::attachDecisions);
        return notices;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notice> allBySubject(String subject) {
        List<Notice> notices = noticeRepository.findBySubjectContainingIgnoreCase(subject);
        notices.forEach(this::attachDecisions);
        return notices;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notice> allByContractType(String contractType) {
        List<Notice> notices = noticeRepository.findByContractTypeIgnoreCase(contractType);
        notices.forEach(this::attachDecisions);
        return notices;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notice> allByProcedureType(String procedureType) {
        List<Notice> notices = noticeRepository.findByProcedureTypeIgnoreCase(procedureType);
        notices.forEach(this::attachDecisions);
        return notices;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notice> allThatHavePlanItem() {
        List<Notice> notices = noticeRepository.findByPlanItemIsNotNull();
        notices.forEach(this::attachDecisions);
        return notices;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notice> allThatDontHavePlanItem() {
        List<Notice> notices = noticeRepository.findByPlanItemIsNull();
        notices.forEach(this::attachDecisions);
        return notices;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notice> allThatHaveDecision() {
        List<Notice> notices = noticeRepository.findAllThatHaveDecision();
        notices.forEach(this::attachDecisions);
        return notices;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notice> allThatDontHaveDecision() {
        List<Notice> notices = noticeRepository.findAllThatDontHaveDecision();
        notices.forEach(this::attachDecisions);
        return notices;
    }

    private PlanItem resolvePlanItem(Long planItemId) {
        if (planItemId == null) {
            return null;
        }

        return planItemRepository.findById(planItemId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "PlanItem not found with id: " + planItemId
                ));
    }

    private void attachDecisions(Notice notice) {
        notice.setDecisions(decisionRepository.findByNoticeId(notice.getId()));
    }
}