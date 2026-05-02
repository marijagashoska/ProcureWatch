package com.procurewatchbackend.service.domain.impl;

import com.procurewatchbackend.service.domain.AnomalyScoreService;
import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.model.entity.Decision;
import com.procurewatchbackend.model.entity.Notice;
import com.procurewatchbackend.model.entity.PlanItem;
import com.procurewatchbackend.model.entity.ProcurementPlan;
import com.procurewatchbackend.model.entity.RealizedContract;
import com.procurewatchbackend.model.entity.RiskAssessment;
import com.procurewatchbackend.model.entity.TriggeredRiskFlag;
import com.procurewatchbackend.model.enums.RiskLevel;
import com.procurewatchbackend.repository.ContractRepository;
import com.procurewatchbackend.repository.NoticeRepository;
import com.procurewatchbackend.repository.RiskAssessmentRepository;
import com.procurewatchbackend.service.domain.RiskAssessmentDomainService;
import com.procurewatchbackend.service.domain.TextSimilarityService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RiskAssessmentDomainServiceImpl implements RiskAssessmentDomainService {

    private static final BigDecimal MAX_RULE_SCORE = new BigDecimal("100.00");
    private static final String MODEL_VERSION = "rule-based-v2";

    private final ContractRepository contractRepository;
    private final NoticeRepository noticeRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final TextSimilarityService textSimilarityService;
    private final AnomalyScoreService anomalyScoreService;

    @Override
    public RiskAssessment evaluateContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found with id: " + contractId));

        Optional<Notice> noticeOptional = resolveNotice(contract);
        Notice notice = noticeOptional.orElse(null);

        Decision decision = contract.getDecision();
        RealizedContract realizedContract = contract.getRealizedContract();
        PlanItem planItem = notice != null ? notice.getPlanItem() : null;
        ProcurementPlan procurementPlan = planItem != null ? planItem.getPlan() : null;

        FeatureSnapshot snapshot = buildFeatureSnapshot(
                contract,
                procurementPlan,
                planItem,
                notice,
                decision,
                realizedContract
        );

        List<FlagEvaluation> evaluations = evaluateRules(snapshot);

        BigDecimal ruleScore = evaluations.stream()
                .map(FlagEvaluation::weight)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .min(MAX_RULE_SCORE)
                .setScale(2, RoundingMode.HALF_UP);

        double similarityScore = textSimilarityService.calculateContractSimilarityScore(contractId);
        BigDecimal similarityScoreBD = new BigDecimal(similarityScore)
                .setScale(2, RoundingMode.HALF_UP);

        RiskAssessment assessment = riskAssessmentRepository.findByContractId(contractId)
                .orElseGet(() -> RiskAssessment.builder().contract(contract).build());

        assessment.getTriggeredFlags().clear();
        assessment.setRuleScore(ruleScore);
        double anomalyScore = anomalyScoreService.calculateAnomalyScore(contractId);
        BigDecimal anomalyScoreBD = new BigDecimal(anomalyScore).setScale(2, RoundingMode.HALF_UP);

        assessment.setSimilarityScore(similarityScoreBD);
        assessment.setAnomalyScore(anomalyScoreBD);

// finalRiskScore = 60% rule + 20% similarity + 20% anomaly
        BigDecimal finalScore = ruleScore.multiply(new BigDecimal("0.60"))
                .add(similarityScoreBD.multiply(new BigDecimal("100")).multiply(new BigDecimal("0.20")))
                .add(anomalyScoreBD.multiply(new BigDecimal("0.20")))
                .min(MAX_RULE_SCORE)
                .setScale(2, RoundingMode.HALF_UP);

        assessment.setFinalRiskScore(finalScore);
        assessment.setRiskLevel(resolveRiskLevel(finalScore));

        assessment.setModelVersion(MODEL_VERSION);
        assessment.setEvaluatedAt(LocalDateTime.now());

        for (FlagEvaluation evaluation : evaluations) {
            assessment.getTriggeredFlags().add(
                    TriggeredRiskFlag.builder()
                            .riskAssessment(assessment)
                            .flagCode(evaluation.flagCode())
                            .flagName(evaluation.flagName())
                            .flagDescription(evaluation.flagDescription())
                            .weight(evaluation.weight().setScale(2, RoundingMode.HALF_UP))
                            .measuredValue(evaluation.measuredValue())
                            .thresholdValue(evaluation.thresholdValue())
                            .createdAt(LocalDateTime.now())
                            .build()
            );
        }

        RiskAssessment saved = riskAssessmentRepository.save(assessment);
        saved.setPriorityRank(calculatePriorityRank(saved));
        return riskAssessmentRepository.save(saved);
    }

    @Override
    public List<RiskAssessment> evaluateAllContracts() {
        return contractRepository.findAll().stream()
                .map(contract -> evaluateContract(contract.getId()))
                .sorted(Comparator.comparing(RiskAssessment::getFinalRiskScore).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RiskAssessment getByContractId(Long contractId) {
        return riskAssessmentRepository.findByContractId(contractId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Risk assessment not found for contract id: " + contractId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RiskAssessment> getByRiskLevel(RiskLevel riskLevel) {
        return riskAssessmentRepository.findByRiskLevelOrderByFinalRiskScoreDesc(riskLevel);
    }

    private Optional<Notice> resolveNotice(Contract contract) {
        if (contract.getNoticeNumber() == null || contract.getNoticeNumber().isBlank()) {
            return Optional.empty();
        }
        return noticeRepository.findFirstByNoticeNumber(contract.getNoticeNumber());
    }

    private FeatureSnapshot buildFeatureSnapshot(
            Contract contract,
            ProcurementPlan procurementPlan,
            PlanItem planItem,
            Notice notice,
            Decision decision,
            RealizedContract realizedContract
    ) {
        LocalDate referenceDate = firstNonNull(
                contract.getContractDate(),
                contract.getPublicationDate(),
                LocalDate.now()
        );

        Long sameSupplierSameInstitutionCount12m = null;

        if (contract.getInstitution() != null && contract.getSupplier() != null) {
            LocalDate fromDate = referenceDate.minusMonths(12);

            if (contract.getContractDate() != null) {
                sameSupplierSameInstitutionCount12m =
                        contractRepository.countByInstitutionIdAndSupplierIdAndContractDateBetween(
                                contract.getInstitution().getId(),
                                contract.getSupplier().getId(),
                                fromDate,
                                referenceDate
                        );
            } else if (contract.getPublicationDate() != null) {
                sameSupplierSameInstitutionCount12m =
                        contractRepository.countByInstitutionIdAndSupplierIdAndPublicationDateBetween(
                                contract.getInstitution().getId(),
                                contract.getSupplier().getId(),
                                fromDate,
                                referenceDate
                        );
            }
        }

        BigDecimal contractToEstimateRatio =
                divide(contract.getContractValueVat(), contract.getEstimatedValueVat());

        BigDecimal realizedToAwardedRatio = realizedContract == null ? null
                : divide(realizedContract.getRealizedValueVat(), realizedContract.getAwardedValueVat());

        BigDecimal paidToRealizedRatio = realizedContract == null ? null
                : divide(realizedContract.getPaidValueVat(), realizedContract.getRealizedValueVat());

        Long daysNoticeToDecision = daysBetween(
                notice != null ? notice.getPublicationDate() : null,
                decision != null ? decision.getDecisionDate() : null
        );

        Long daysDecisionToContract = daysBetween(
                decision != null ? decision.getDecisionDate() : null,
                contract.getContractDate()
        );

        Long daysNoticeToContract = daysBetween(
                notice != null ? notice.getPublicationDate() : null,
                contract.getContractDate()
        );

        boolean missingNoticeLink = notice == null;
        boolean missingPlanItemLink = notice != null && notice.getPlanItem() == null;
        boolean missingProcurementPlanLink = planItem != null && procurementPlan == null;

        return new FeatureSnapshot(
                contract,
                procurementPlan,
                planItem,
                notice,
                decision,
                realizedContract,
                contractToEstimateRatio,
                sameSupplierSameInstitutionCount12m,
                realizedToAwardedRatio,
                paidToRealizedRatio,
                daysNoticeToDecision,
                daysDecisionToContract,
                daysNoticeToContract,
                missingNoticeLink,
                missingPlanItemLink,
                missingProcurementPlanLink
        );
    }

    private List<FlagEvaluation> evaluateRules(FeatureSnapshot snapshot) {
        List<FlagEvaluation> flags = new ArrayList<>();

        if (isGreaterThan(snapshot.contractToEstimateRatio(), new BigDecimal("1.50"))) {
            flags.add(new FlagEvaluation(
                    "HIGH_CONTRACT_TO_ESTIMATE_RATIO",
                    "High contract-to-estimate ratio",
                    "The contract value is much higher than the estimated value.",
                    new BigDecimal("25.00"),
                    formatDecimal(snapshot.contractToEstimateRatio()),
                    "> 1.50"
            ));
        }

        if (snapshot.sameSupplierSameInstitutionCount12m() != null
                && snapshot.sameSupplierSameInstitutionCount12m() >= 4) {
            flags.add(new FlagEvaluation(
                    "REPEATED_SUPPLIER_SAME_INSTITUTION_12M",
                    "Repeated supplier with same institution",
                    "The same supplier received several contracts from the same institution in the last 12 months.",
                    new BigDecimal("15.00"),
                    snapshot.sameSupplierSameInstitutionCount12m().toString(),
                    ">= 4 contracts / 12 months"
            ));
        }

        if (snapshot.missingNoticeLink()) {
            flags.add(new FlagEvaluation(
                    "MISSING_NOTICE_LINK",
                    "Missing notice linkage",
                    "The contract could not be linked to a notice record.",
                    new BigDecimal("12.00"),
                    String.valueOf(snapshot.contract().getNoticeNumber()),
                    "notice must exist"
            ));
        }

        if (!snapshot.missingNoticeLink() && snapshot.missingPlanItemLink()) {
            flags.add(new FlagEvaluation(
                    "MISSING_PLAN_ITEM_LINK",
                    "Missing plan item linkage",
                    "The linked notice does not point to a procurement plan item.",
                    new BigDecimal("10.00"),
                    snapshot.notice().getId().toString(),
                    "notice.planItem must exist"
            ));
        }

        if (snapshot.decision() == null) {
            flags.add(new FlagEvaluation(
                    "CONTRACT_WITHOUT_DECISION",
                    "Contract without decision",
                    "The contract does not have a linked decision record.",
                    new BigDecimal("12.00"),
                    "missing",
                    "decision should exist"
            ));
        }

        if (snapshot.realizedContract() != null && snapshot.realizedToAwardedRatio() != null) {
            if (isGreaterThan(snapshot.realizedToAwardedRatio(), new BigDecimal("1.20"))) {
                flags.add(new FlagEvaluation(
                        "REALIZED_VALUE_ABOVE_AWARDED",
                        "Realized value above awarded",
                        "The realized value is significantly above the awarded value.",
                        new BigDecimal("18.00"),
                        formatDecimal(snapshot.realizedToAwardedRatio()),
                        "> 1.20"
                ));
            } else if (isLessThan(snapshot.realizedToAwardedRatio(), new BigDecimal("0.50"))) {
                flags.add(new FlagEvaluation(
                        "REALIZED_VALUE_BELOW_AWARDED",
                        "Realized value below awarded",
                        "The realized value is significantly below the awarded value.",
                        new BigDecimal("14.00"),
                        formatDecimal(snapshot.realizedToAwardedRatio()),
                        "< 0.50"
                ));
            }
        }

        if (snapshot.realizedContract() != null
                && snapshot.paidToRealizedRatio() != null
                && snapshot.paidToRealizedRatio().subtract(BigDecimal.ONE).abs()
                .compareTo(new BigDecimal("0.01")) > 0) {
            flags.add(new FlagEvaluation(
                    "PAID_TO_REALIZED_MISMATCH",
                    "Paid value mismatch",
                    "The paid amount does not match the realized amount.",
                    new BigDecimal("10.00"),
                    formatDecimal(snapshot.paidToRealizedRatio()),
                    "1.00 ± 0.01"
            ));
        }

        if (snapshot.daysNoticeToDecision() != null && snapshot.daysNoticeToDecision() > 60) {
            flags.add(new FlagEvaluation(
                    "LONG_NOTICE_TO_DECISION_DELAY",
                    "Long notice-to-decision delay",
                    "There is an unusually long delay between the notice and the decision.",
                    new BigDecimal("8.00"),
                    snapshot.daysNoticeToDecision() + " days",
                    "> 60 days"
            ));
        }

        if (snapshot.daysDecisionToContract() != null && snapshot.daysDecisionToContract() > 30) {
            flags.add(new FlagEvaluation(
                    "LONG_DECISION_TO_CONTRACT_DELAY",
                    "Long decision-to-contract delay",
                    "There is an unusually long delay between the decision and the contract date.",
                    new BigDecimal("6.00"),
                    snapshot.daysDecisionToContract() + " days",
                    "> 30 days"
            ));
        }

        if (snapshot.daysNoticeToContract() != null && snapshot.daysNoticeToContract() > 90) {
            flags.add(new FlagEvaluation(
                    "LONG_NOTICE_TO_CONTRACT_DELAY",
                    "Long notice-to-contract delay",
                    "There is an unusually long delay between the notice and the contract date.",
                    new BigDecimal("7.00"),
                    snapshot.daysNoticeToContract() + " days",
                    "> 90 days"
            ));
        }

        if (snapshot.planItem() != null && Boolean.FALSE.equals(snapshot.planItem().getHasNotice())) {
            flags.add(new FlagEvaluation(
                    "PLAN_ITEM_MARKED_WITHOUT_NOTICE",
                    "Plan item says no notice",
                    "The linked plan item is marked as having no notice even though the contract exists.",
                    new BigDecimal("5.00"),
                    "hasNotice=false",
                    "hasNotice=true"
            ));
        }

        return flags;
    }

    private Integer calculatePriorityRank(RiskAssessment assessment) {
        BigDecimal score = assessment.getFinalRiskScore() == null
                ? BigDecimal.ZERO
                : assessment.getFinalRiskScore();

        return Math.toIntExact(riskAssessmentRepository.countByFinalRiskScoreGreaterThan(score) + 1);
    }

    private RiskLevel resolveRiskLevel(BigDecimal score) {
        if (score.compareTo(new BigDecimal("70.00")) >= 0) {
            return RiskLevel.CRITICAL;
        }
        if (score.compareTo(new BigDecimal("45.00")) >= 0) {
            return RiskLevel.HIGH;
        }
        if (score.compareTo(new BigDecimal("20.00")) >= 0) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    private BigDecimal divide(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return numerator.divide(denominator, 4, RoundingMode.HALF_UP);
    }

    private boolean isGreaterThan(BigDecimal value, BigDecimal threshold) {
        return value != null && value.compareTo(threshold) > 0;
    }

    private boolean isLessThan(BigDecimal value, BigDecimal threshold) {
        return value != null && value.compareTo(threshold) < 0;
    }

    private Long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(start, end);
    }

    private String formatDecimal(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    @SafeVarargs
    private <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private record FeatureSnapshot(
            Contract contract,
            ProcurementPlan procurementPlan,
            PlanItem planItem,
            Notice notice,
            Decision decision,
            RealizedContract realizedContract,
            BigDecimal contractToEstimateRatio,
            Long sameSupplierSameInstitutionCount12m,
            BigDecimal realizedToAwardedRatio,
            BigDecimal paidToRealizedRatio,
            Long daysNoticeToDecision,
            Long daysDecisionToContract,
            Long daysNoticeToContract,
            boolean missingNoticeLink,
            boolean missingPlanItemLink,
            boolean missingProcurementPlanLink
    ) {
    }

    private record FlagEvaluation(
            String flagCode,
            String flagName,
            String flagDescription,
            BigDecimal weight,
            String measuredValue,
            String thresholdValue
    ) {
    }
}