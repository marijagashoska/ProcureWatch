package com.procurewatchbackend.service.domain.impl;

import com.procurewatchbackend.dto.create.CreateProcurementPlanDto;
import com.procurewatchbackend.dto.edit.EditProcurementPlanDto;
import com.procurewatchbackend.model.entity.Institution;
import com.procurewatchbackend.model.entity.ProcurementPlan;
import com.procurewatchbackend.repository.InstitutionRepository;
import com.procurewatchbackend.repository.PlanItemRepository;
import com.procurewatchbackend.repository.ProcurementPlanRepository;
import com.procurewatchbackend.service.domain.ProcurementPlanDomainService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProcurementPlanDomainServiceImpl implements ProcurementPlanDomainService {

    private final ProcurementPlanRepository procurementPlanRepository;
    private final InstitutionRepository institutionRepository;
    private final PlanItemRepository planItemRepository;

    @Override
    public ProcurementPlan add(CreateProcurementPlanDto dto) {
        Institution institution = institutionRepository.findById(dto.institutionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Institution not found with id: " + dto.institutionId()
                ));

        ProcurementPlan plan = new ProcurementPlan();
        plan.setInstitution(institution);
        plan.setPlanYear(dto.planYear());
        plan.setPublicationDate(dto.publicationDate());
        plan.setSourceUrl(dto.sourceUrl());

        ProcurementPlan savedPlan = procurementPlanRepository.save(plan);
        savedPlan.setPlanItems(planItemRepository.findByPlanId(savedPlan.getId()));

        return savedPlan;
    }

    @Override
    public ProcurementPlan edit(Long id, EditProcurementPlanDto dto) {
        ProcurementPlan existingPlan = procurementPlanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Procurement plan not found with id: " + id
                ));

        Institution institution = institutionRepository.findById(dto.institutionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Institution not found with id: " + dto.institutionId()
                ));

        existingPlan.setInstitution(institution);
        existingPlan.setPlanYear(dto.planYear());
        existingPlan.setPublicationDate(dto.publicationDate());
        existingPlan.setSourceUrl(dto.sourceUrl());

        ProcurementPlan updatedPlan = procurementPlanRepository.save(existingPlan);
        updatedPlan.setPlanItems(planItemRepository.findByPlanId(updatedPlan.getId()));

        return updatedPlan;
    }

    @Override
    public void delete(Long id) {
        ProcurementPlan existingPlan = procurementPlanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Procurement plan not found with id: " + id
                ));

        planItemRepository.deleteByPlanId(existingPlan.getId());
        procurementPlanRepository.delete(existingPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcurementPlan> getAll() {
        List<ProcurementPlan> plans = procurementPlanRepository.findAll();
        plans.forEach(this::attachPlanItems);
        return plans;
    }

    @Override
    @Transactional(readOnly = true)
    public ProcurementPlan getById(Long id) {
        ProcurementPlan plan = procurementPlanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Procurement plan not found with id: " + id
                ));

        attachPlanItems(plan);
        return plan;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcurementPlan> getByInstitutionId(Long institutionId) {
        List<ProcurementPlan> plans = procurementPlanRepository.findByInstitutionId(institutionId);
        plans.forEach(this::attachPlanItems);
        return plans;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcurementPlan> getByYear(Integer year) {
        List<ProcurementPlan> plans = procurementPlanRepository.findByPlanYear(year);
        plans.forEach(this::attachPlanItems);
        return plans;
    }

    private void attachPlanItems(ProcurementPlan plan) {
        plan.setPlanItems(planItemRepository.findByPlanId(plan.getId()));
    }
}