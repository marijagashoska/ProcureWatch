package com.procurewatchbackend.service.domain.impl;

import com.procurewatchbackend.dto.create.CreateDecisionDto;
import com.procurewatchbackend.dto.edit.EditDecisionDto;
import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.model.entity.Decision;
import com.procurewatchbackend.model.entity.Institution;
import com.procurewatchbackend.model.entity.Notice;
import com.procurewatchbackend.model.entity.Supplier;
import com.procurewatchbackend.repository.ContractRepository;
import com.procurewatchbackend.repository.DecisionRepository;
import com.procurewatchbackend.repository.InstitutionRepository;
import com.procurewatchbackend.repository.NoticeRepository;
import com.procurewatchbackend.repository.SupplierRepository;
import com.procurewatchbackend.service.domain.DecisionDomainService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DecisionDomainServiceImpl implements DecisionDomainService {

    private final DecisionRepository decisionRepository;
    private final NoticeRepository noticeRepository;
    private final ContractRepository contractRepository;
    private final InstitutionRepository institutionRepository;
    private final SupplierRepository supplierRepository;

    @Override
    public Decision add(CreateDecisionDto dto) {
        Notice notice = noticeRepository.findById(dto.noticeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Notice not found with id: " + dto.noticeId()
                ));

        Contract contract = contractRepository.findById(dto.contractId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Contract not found with id: " + dto.contractId()
                ));

        Institution institution = institutionRepository.findById(dto.institutionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Institution not found with id: " + dto.institutionId()
                ));

        Supplier supplier = resolveSupplier(dto.supplierId());

        Decision decision = new Decision();
        decision.setNotice(notice);
        decision.setContract(contract);
        decision.setInstitution(institution);
        decision.setSupplier(supplier);
        decision.setNoticeNumber(dto.noticeNumber());
        decision.setDecisionDate(dto.decisionDate());
        decision.setSubject(dto.subject());
        decision.setDecisionText(dto.decisionText());
        decision.setProcedureType(dto.procedureType());
        decision.setSourceUrl(dto.sourceUrl());

        return decisionRepository.save(decision);
    }

    @Override
    public Decision edit(Long id, EditDecisionDto dto) {
        Decision existing = decisionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Decision not found with id: " + id
                ));

        Notice notice = noticeRepository.findById(dto.noticeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Notice not found with id: " + dto.noticeId()
                ));

        Contract contract = contractRepository.findById(dto.contractId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Contract not found with id: " + dto.contractId()
                ));

        Institution institution = institutionRepository.findById(dto.institutionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Institution not found with id: " + dto.institutionId()
                ));

        Supplier supplier = resolveSupplier(dto.supplierId());

        existing.setNotice(notice);
        existing.setContract(contract);
        existing.setInstitution(institution);
        existing.setSupplier(supplier);
        existing.setNoticeNumber(dto.noticeNumber());
        existing.setDecisionDate(dto.decisionDate());
        existing.setSubject(dto.subject());
        existing.setDecisionText(dto.decisionText());
        existing.setProcedureType(dto.procedureType());
        existing.setSourceUrl(dto.sourceUrl());

        return decisionRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Decision existing = decisionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Decision not found with id: " + id
                ));

        decisionRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Decision> getAllForInstitution(Long institutionId) {
        return decisionRepository.findByInstitutionId(institutionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Decision> getAllForSuplier(Long supplierId) {
        return decisionRepository.findBySupplierId(supplierId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Decision> getAllByProcedureType(String procedureType) {
        return decisionRepository.findByProcedureTypeIgnoreCase(procedureType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Decision> getAllBySubject(String subject) {
        return decisionRepository.findBySubjectContainingIgnoreCase(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Decision> getAllByDecisionDate(LocalDate decisionDate) {
        return decisionRepository.findByDecisionDate(decisionDate);
    }

    private Supplier resolveSupplier(Long supplierId) {
        if (supplierId == null) {
            return null;
        }

        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Supplier not found with id: " + supplierId
                ));
    }


    @Override
    @Transactional(readOnly = true)
    public List<Decision> getAllByNotice(Long noticeId) {
        return decisionRepository.findByNoticeId(noticeId);
    }
}