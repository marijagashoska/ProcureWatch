package com.procurewatchbackend.service.domain.impl;

import com.procurewatchbackend.dto.create.CreatePlanItemDto;
import com.procurewatchbackend.dto.edit.EditPlanItemDto;
import com.procurewatchbackend.model.entity.Institution;
import com.procurewatchbackend.model.entity.PlanItem;
import com.procurewatchbackend.model.entity.ProcurementPlan;
import com.procurewatchbackend.repository.PlanItemRepository;
import com.procurewatchbackend.repository.ProcurementPlanRepository;
import com.procurewatchbackend.service.domain.PlanItemDomainService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanItemDomainServiceImpl implements PlanItemDomainService {

    private final PlanItemRepository planItemRepository;
    private final ProcurementPlanRepository procurementPlanRepository;

    @Override
    public PlanItem add(CreatePlanItemDto dto) {
        ProcurementPlan plan = procurementPlanRepository.findById(dto.planId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Procurement plan not found with id: " + dto.planId()
                ));

        PlanItem item = new PlanItem();
        item.setPlan(plan);
        item.setSubject(dto.subject());
        item.setCpvCode(dto.cpvCode());
        item.setContractType(dto.contractType());
        item.setProcedureType(dto.procedureType());
        item.setExpectedStartMonth(dto.expectedStartMonth());
        item.setHasNotice(dto.hasNotice());
        item.setNotes(dto.notes());
        item.setSourceUrl(dto.sourceUrl());

        return planItemRepository.save(item);
    }

    @Override
    public PlanItem edit(Long id, EditPlanItemDto dto) {
        PlanItem existingItem = planItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Plan item not found with id: " + id
                ));

        ProcurementPlan plan = procurementPlanRepository.findById(dto.planId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Procurement plan not found with id: " + dto.planId()
                ));

        existingItem.setPlan(plan);
        existingItem.setSubject(dto.subject());
        existingItem.setCpvCode(dto.cpvCode());
        existingItem.setContractType(dto.contractType());
        existingItem.setProcedureType(dto.procedureType());
        existingItem.setExpectedStartMonth(dto.expectedStartMonth());
        existingItem.setHasNotice(dto.hasNotice());
        existingItem.setNotes(dto.notes());
        existingItem.setSourceUrl(dto.sourceUrl());

        return planItemRepository.save(existingItem);
    }

    @Override
    public void delete(Long id) {
        PlanItem existingItem = planItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Plan item not found with id: " + id
                ));

        planItemRepository.delete(existingItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanItem> getAll() {
        return planItemRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public PlanItem getById(Long id) {
        return planItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Plan item not found with id: " + id
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanItem> getByPlanId(Long planId) {
        return planItemRepository.findByPlanId(planId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PlanItem> getAllPaginated(Pageable pageable) {
        return planItemRepository.findAll(pageable);
    }
}