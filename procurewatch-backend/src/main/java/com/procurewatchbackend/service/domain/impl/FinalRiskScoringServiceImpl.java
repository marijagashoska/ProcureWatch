package com.procurewatchbackend.service.domain.impl;

import com.procurewatchbackend.model.entity.RiskAssessment;
import com.procurewatchbackend.model.entity.TriggeredRiskFlag;
import com.procurewatchbackend.model.enums.RiskLevel;
import com.procurewatchbackend.repository.RiskAssessmentRepository;
import com.procurewatchbackend.service.domain.FinalRiskScoringService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class FinalRiskScoringServiceImpl implements FinalRiskScoringService {

    private static final String MODEL_VERSION = "final-risk-v1";

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal MAX_SCORE = new BigDecimal("100.00");

    private static final BigDecimal RULE_WEIGHT = new BigDecimal("0.60");
    private static final BigDecimal ANOMALY_WEIGHT = new BigDecimal("0.20");
    private static final BigDecimal SIMILARITY_WEIGHT = new BigDecimal("0.15");
    private static final BigDecimal CLUSTER_WEIGHT = new BigDecimal("0.05");

    private static final BigDecimal FLAG_COUNT_BONUS_PER_FLAG = new BigDecimal("1.25");
    private static final BigDecimal MAX_FLAG_COUNT_BONUS = new BigDecimal("8.00");

    private static final BigDecimal SEVERE_FLAG_WEIGHT_THRESHOLD = new BigDecimal("15.00");
    private static final BigDecimal SEVERE_FLAG_BONUS_PER_FLAG = new BigDecimal("2.50");
    private static final BigDecimal MAX_SEVERE_FLAG_BONUS = new BigDecimal("7.00");

    private static final BigDecimal MAX_TOTAL_FLAG_BONUS = new BigDecimal("15.00");

    private final RiskAssessmentRepository riskAssessmentRepository;

    @Override
    public RiskAssessment calculateAndApply(RiskAssessment assessment) {
        if (assessment == null) {
            throw new IllegalArgumentException("RiskAssessment must not be null.");
        }

        BigDecimal weightedBaseScore = calculateWeightedBaseScore(assessment);
        BigDecimal flagBonus = calculateFlagBonus(assessment.getTriggeredFlags());

        BigDecimal finalRiskScore = weightedBaseScore
                .add(flagBonus)
                .max(ZERO)
                .min(MAX_SCORE)
                .setScale(2, RoundingMode.HALF_UP);

        assessment.setFinalRiskScore(finalRiskScore);
        assessment.setRiskLevel(resolveRiskLevel(finalRiskScore));
        assessment.setModelVersion(MODEL_VERSION);
        assessment.setEvaluatedAt(LocalDateTime.now());

        return assessment;
    }

    @Override
    public RiskAssessment recalculateExistingAssessment(Long contractId) {
        RiskAssessment assessment = riskAssessmentRepository.findByContractId(contractId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Risk assessment not found for contract id: " + contractId
                ));

        calculateAndApply(assessment);

        RiskAssessment saved = riskAssessmentRepository.save(assessment);
        recalculatePriorityRanks();

        return riskAssessmentRepository.findById(saved.getId()).orElse(saved);
    }

    @Override
    public List<RiskAssessment> recalculateAllExistingAssessments() {
        List<RiskAssessment> assessments = riskAssessmentRepository.findAll();

        assessments.forEach(this::calculateAndApply);

        riskAssessmentRepository.saveAll(assessments);
        recalculatePriorityRanks();

        return riskAssessmentRepository.findAll()
                .stream()
                .filter(assessment -> assessment.getFinalRiskScore() != null)
                .sorted(Comparator.comparing(
                        RiskAssessment::getFinalRiskScore,
                        Comparator.nullsLast(BigDecimal::compareTo)
                ).reversed())
                .toList();
    }

    @Override
    public void recalculatePriorityRanks() {
        List<RiskAssessment> sortedAssessments = riskAssessmentRepository.findAll()
                .stream()
                .filter(assessment -> assessment.getFinalRiskScore() != null)
                .sorted(Comparator.comparing(
                        RiskAssessment::getFinalRiskScore,
                        Comparator.nullsLast(BigDecimal::compareTo)
                ).reversed())
                .toList();

        BigDecimal previousScore = null;
        int currentRank = 0;

        for (int index = 0; index < sortedAssessments.size(); index++) {
            RiskAssessment assessment = sortedAssessments.get(index);
            BigDecimal score = assessment.getFinalRiskScore();

            if (previousScore == null || score.compareTo(previousScore) != 0) {
                currentRank = index + 1;
                previousScore = score;
            }

            assessment.setPriorityRank(currentRank);
        }

        riskAssessmentRepository.saveAll(sortedAssessments);
    }

    @Override
    public RiskLevel resolveRiskLevel(BigDecimal finalRiskScore) {
        BigDecimal score = finalRiskScore == null ? ZERO : finalRiskScore;

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

    private BigDecimal calculateWeightedBaseScore(RiskAssessment assessment) {
        List<WeightedScore> scores = List.of(
                        new WeightedScore(normalizePercentageScore(assessment.getRuleScore()), RULE_WEIGHT),
                        new WeightedScore(normalizeFlexibleScore(assessment.getAnomalyScore()), ANOMALY_WEIGHT),
                        new WeightedScore(normalizeFlexibleScore(assessment.getSimilarityScore()), SIMILARITY_WEIGHT),
                        new WeightedScore(normalizeFlexibleScore(assessment.getClusterScore()), CLUSTER_WEIGHT)
                )
                .stream()
                .filter(weightedScore -> weightedScore.score() != null)
                .toList();

        if (scores.isEmpty()) {
            return ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal activeWeightTotal = scores.stream()
                .map(WeightedScore::weight)
                .reduce(ZERO, BigDecimal::add);

        return scores.stream()
                .map(weightedScore -> weightedScore.score()
                        .multiply(weightedScore.weight())
                        .divide(activeWeightTotal, 6, RoundingMode.HALF_UP))
                .reduce(ZERO, BigDecimal::add)
                .max(ZERO)
                .min(MAX_SCORE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateFlagBonus(List<TriggeredRiskFlag> flags) {
        if (flags == null || flags.isEmpty()) {
            return ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        long flagCount = flags.size();

        long severeFlagCount = flags.stream()
                .map(TriggeredRiskFlag::getWeight)
                .filter(Objects::nonNull)
                .filter(weight -> weight.compareTo(SEVERE_FLAG_WEIGHT_THRESHOLD) >= 0)
                .count();

        BigDecimal flagCountBonus = BigDecimal.valueOf(flagCount)
                .multiply(FLAG_COUNT_BONUS_PER_FLAG)
                .min(MAX_FLAG_COUNT_BONUS);

        BigDecimal severeFlagBonus = BigDecimal.valueOf(severeFlagCount)
                .multiply(SEVERE_FLAG_BONUS_PER_FLAG)
                .min(MAX_SEVERE_FLAG_BONUS);

        return flagCountBonus
                .add(severeFlagBonus)
                .min(MAX_TOTAL_FLAG_BONUS)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizePercentageScore(BigDecimal score) {
        if (score == null) {
            return null;
        }

        return score
                .max(ZERO)
                .min(MAX_SCORE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeFlexibleScore(BigDecimal score) {
        if (score == null) {
            return null;
        }

        BigDecimal safeScore = score.max(ZERO);

        if (safeScore.compareTo(ONE) <= 0) {
            return safeScore
                    .multiply(MAX_SCORE)
                    .min(MAX_SCORE)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return safeScore
                .min(MAX_SCORE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private record WeightedScore(
            BigDecimal score,
            BigDecimal weight
    ) {
    }
}