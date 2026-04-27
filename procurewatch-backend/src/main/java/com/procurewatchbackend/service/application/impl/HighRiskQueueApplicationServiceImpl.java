package com.procurewatchbackend.service.application.impl;

import com.procurewatchbackend.dto.display.PagedResponseDto;
import com.procurewatchbackend.dto.display.queue.HighRiskQueueItemDto;
import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.model.entity.RiskAssessment;
import com.procurewatchbackend.model.entity.TriggeredRiskFlag;
import com.procurewatchbackend.model.enums.RiskLevel;
import com.procurewatchbackend.repository.RiskAssessmentRepository;
import com.procurewatchbackend.service.application.HighRiskQueueApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HighRiskQueueApplicationServiceImpl implements HighRiskQueueApplicationService {

    private final RiskAssessmentRepository riskAssessmentRepository;

    @Override
    public PagedResponseDto<HighRiskQueueItemDto> getQueue(
            RiskLevel riskLevel,
            Long institutionId,
            Long supplierId,
            LocalDate dateFrom,
            LocalDate dateTo,
            BigDecimal minValue,
            BigDecimal maxValue,
            String flagCode,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 100));

        List<RiskAssessment> filtered = riskAssessmentRepository.findAll()
                .stream()
                .filter(assessment -> assessment.getContract() != null)
                .filter(assessment -> matchesRiskLevel(assessment, riskLevel))
                .filter(assessment -> matchesInstitution(assessment, institutionId))
                .filter(assessment -> matchesSupplier(assessment, supplierId))
                .filter(assessment -> matchesDateRange(assessment, dateFrom, dateTo))
                .filter(assessment -> matchesValueRange(assessment, minValue, maxValue))
                .filter(assessment -> matchesFlagCode(assessment, flagCode))
                .sorted(resolveComparator(sortBy, sortDir))
                .toList();

        long totalElements = filtered.size();
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);

        int fromIndex = safePage * safeSize;
        int toIndex = Math.min(fromIndex + safeSize, filtered.size());

        List<HighRiskQueueItemDto> content = fromIndex >= filtered.size()
                ? List.of()
                : filtered.subList(fromIndex, toIndex)
                .stream()
                .map(this::mapToQueueItem)
                .toList();

        return new PagedResponseDto<>(
                content,
                safePage,
                safeSize,
                totalElements,
                totalPages,
                safePage == 0,
                safePage >= totalPages - 1,
                sortBy,
                sortDir
        );
    }

    private boolean matchesRiskLevel(RiskAssessment assessment, RiskLevel requestedRiskLevel) {
        RiskLevel actualRiskLevel = assessment.getRiskLevel();

        if (actualRiskLevel == null) {
            return false;
        }

        if (requestedRiskLevel != null) {
            return actualRiskLevel == requestedRiskLevel;
        }

        return actualRiskLevel == RiskLevel.HIGH || actualRiskLevel == RiskLevel.CRITICAL;
    }

    private boolean matchesInstitution(RiskAssessment assessment, Long institutionId) {
        if (institutionId == null) {
            return true;
        }

        Contract contract = assessment.getContract();

        return contract.getInstitution() != null
                && Objects.equals(contract.getInstitution().getId(), institutionId);
    }

    private boolean matchesSupplier(RiskAssessment assessment, Long supplierId) {
        if (supplierId == null) {
            return true;
        }

        Contract contract = assessment.getContract();

        return contract.getSupplier() != null
                && Objects.equals(contract.getSupplier().getId(), supplierId);
    }

    private boolean matchesDateRange(
            RiskAssessment assessment,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        if (dateFrom == null && dateTo == null) {
            return true;
        }

        LocalDate contractDate = assessment.getContract().getContractDate();

        if (contractDate == null) {
            contractDate = assessment.getContract().getPublicationDate();
        }

        if (contractDate == null) {
            return false;
        }

        if (dateFrom != null && contractDate.isBefore(dateFrom)) {
            return false;
        }

        return dateTo == null || !contractDate.isAfter(dateTo);
    }

    private boolean matchesValueRange(
            RiskAssessment assessment,
            BigDecimal minValue,
            BigDecimal maxValue
    ) {
        BigDecimal value = assessment.getContract().getContractValueVat();

        if (minValue == null && maxValue == null) {
            return true;
        }

        if (value == null) {
            return false;
        }

        if (minValue != null && value.compareTo(minValue) < 0) {
            return false;
        }

        return maxValue == null || value.compareTo(maxValue) <= 0;
    }

    private boolean matchesFlagCode(RiskAssessment assessment, String flagCode) {
        if (flagCode == null || flagCode.isBlank()) {
            return true;
        }

        if (assessment.getTriggeredFlags() == null) {
            return false;
        }

        String normalizedFlagCode = flagCode.trim();

        return assessment.getTriggeredFlags()
                .stream()
                .anyMatch(flag -> flag.getFlagCode() != null
                        && flag.getFlagCode().equalsIgnoreCase(normalizedFlagCode));
    }

    private Comparator<RiskAssessment> resolveComparator(String sortBy, String sortDir) {
        String safeSortBy = sortBy == null || sortBy.isBlank()
                ? "priorityRank"
                : sortBy;

        Comparator<RiskAssessment> comparator = switch (safeSortBy) {
            case "score", "finalRiskScore" -> Comparator.comparing(
                    RiskAssessment::getFinalRiskScore,
                    Comparator.nullsLast(BigDecimal::compareTo)
            );
            case "contractDate" -> Comparator.comparing(
                    assessment -> resolveContractDate(assessment.getContract()),
                    Comparator.nullsLast(LocalDate::compareTo)
            );
            case "value", "contractValue" -> Comparator.comparing(
                    assessment -> assessment.getContract().getContractValueVat(),
                    Comparator.nullsLast(BigDecimal::compareTo)
            );
            case "riskLevel" -> Comparator.comparing(
                    RiskAssessment::getRiskLevel,
                    Comparator.nullsLast(Enum::compareTo)
            );
            case "flagCount" -> Comparator.comparingInt(
                    assessment -> assessment.getTriggeredFlags() == null
                            ? 0
                            : assessment.getTriggeredFlags().size()
            );
            case "priorityRank" -> Comparator.comparing(
                    RiskAssessment::getPriorityRank,
                    Comparator.nullsLast(Integer::compareTo)
            );
            default -> Comparator.comparing(
                    RiskAssessment::getPriorityRank,
                    Comparator.nullsLast(Integer::compareTo)
            );
        };

        boolean descending = "desc".equalsIgnoreCase(sortDir);

        if ("priorityRank".equals(safeSortBy)) {
            return descending ? comparator.reversed() : comparator;
        }

        return descending ? comparator.reversed() : comparator;
    }

    private LocalDate resolveContractDate(Contract contract) {
        if (contract == null) {
            return null;
        }

        return contract.getContractDate() != null
                ? contract.getContractDate()
                : contract.getPublicationDate();
    }

    private HighRiskQueueItemDto mapToQueueItem(RiskAssessment assessment) {
        Contract contract = assessment.getContract();

        List<String> flagNames = assessment.getTriggeredFlags() == null
                ? List.of()
                : assessment.getTriggeredFlags()
                .stream()
                .map(TriggeredRiskFlag::getFlagName)
                .filter(Objects::nonNull)
                .toList();

        String explanationPreview = buildExplanationPreview(assessment, flagNames);

        return new HighRiskQueueItemDto(
                contract.getId(),
                contract.getNoticeNumber(),
                contract.getSubject(),

                contract.getInstitution() != null ? contract.getInstitution().getId() : null,
                contract.getInstitution() != null ? contract.getInstitution().getOfficialName() : null,

                contract.getSupplier() != null ? contract.getSupplier().getId() : null,
                contract.getSupplier() != null ? contract.getSupplier().getOfficialName() : null,

                contract.getContractValueVat(),
                resolveContractDate(contract),

                assessment.getFinalRiskScore(),
                assessment.getRiskLevel(),
                assessment.getPriorityRank(),

                flagNames.size(),
                flagNames,
                explanationPreview
        );
    }

    private String buildExplanationPreview(
            RiskAssessment assessment,
            List<String> flagNames
    ) {
        String riskLevel = assessment.getRiskLevel() == null
                ? "UNKNOWN"
                : assessment.getRiskLevel().name();

        String score = assessment.getFinalRiskScore() == null
                ? "not calculated"
                : assessment.getFinalRiskScore().toPlainString();

        if (flagNames.isEmpty()) {
            return "Risk level " + riskLevel + " with score " + score + ". No triggered flags saved.";
        }

        return "Risk level " + riskLevel
                + " with score " + score
                + ". Main flags: " + String.join(", ", flagNames);
    }
}