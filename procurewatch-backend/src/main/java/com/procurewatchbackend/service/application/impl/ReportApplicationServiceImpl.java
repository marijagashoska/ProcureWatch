package com.procurewatchbackend.service.application.impl;

import com.procurewatchbackend.dto.report.ContractReportDto;
import com.procurewatchbackend.dto.report.HighRiskContractReportDto;
import com.procurewatchbackend.dto.report.ReportLifecycleItemDto;
import com.procurewatchbackend.model.entity.AIExplanation;
import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.model.entity.Decision;
import com.procurewatchbackend.model.entity.RealizedContract;
import com.procurewatchbackend.model.entity.RiskAssessment;
import com.procurewatchbackend.model.entity.TriggeredRiskFlag;
import com.procurewatchbackend.repository.AIExplanationRepository;
import com.procurewatchbackend.repository.ContractRepository;
import com.procurewatchbackend.repository.RiskAssessmentRepository;
import com.procurewatchbackend.service.application.ReportApplicationService;
import com.procurewatchbackend.util.CsvExportUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportApplicationServiceImpl implements ReportApplicationService {

    private final ContractRepository contractRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final AIExplanationRepository aiExplanationRepository;

    @Override
    public ContractReportDto getContractReport(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found with id: " + contractId));

        RiskAssessment riskAssessment = findRiskAssessment(contract);
        AIExplanation explanation = findLatestExplanation(contract.getId());

        return mapToContractReport(contract, riskAssessment, explanation);
    }

    @Override
    public List<HighRiskContractReportDto> getHighRiskContractsReport() {
        return riskAssessmentRepository.findAll()
                .stream()
                .filter(this::isHighRisk)
                .filter(riskAssessment -> riskAssessment.getContract() != null)
                .map(riskAssessment -> {
                    Contract contract = riskAssessment.getContract();
                    AIExplanation explanation = findLatestExplanation(contract.getId());
                    return mapToHighRiskReport(contract, riskAssessment, explanation);
                })
                .toList();
    }

    @Override
    public String exportContractReportCsv(Long contractId) {
        ContractReportDto report = getContractReport(contractId);

        StringBuilder csv = new StringBuilder();

        csv.append("contractId,noticeNumber,subject,institutionName,supplierName,contractDate,contractValueVat,currency,finalRiskScore,riskLevel,explanationText\n");

        csv.append(csvValue(report.getContractId())).append(",");
        csv.append(csvValue(report.getNoticeNumber())).append(",");
        csv.append(csvValue(report.getSubject())).append(",");
        csv.append(csvValue(report.getInstitutionName())).append(",");
        csv.append(csvValue(report.getSupplierName())).append(",");
        csv.append(csvValue(report.getContractDate())).append(",");
        csv.append(csvValue(report.getContractValueVat())).append(",");
        csv.append(csvValue(report.getCurrency())).append(",");
        csv.append(csvValue(report.getFinalRiskScore())).append(",");
        csv.append(csvValue(report.getRiskLevel())).append(",");
        csv.append(csvValue(report.getExplanationText())).append("\n");

        return csv.toString();
    }

    @Override
    public String exportHighRiskContractsCsv() {
        List<HighRiskContractReportDto> reports = getHighRiskContractsReport();

        StringBuilder csv = new StringBuilder();

        csv.append("contractId,subject,institutionName,supplierName,contractDate,contractValueVat,currency,finalRiskScore,riskLevel,priorityRank,explanationText\n");

        for (HighRiskContractReportDto report : reports) {
            csv.append(csvValue(report.getContractId())).append(",");
            csv.append(csvValue(report.getSubject())).append(",");
            csv.append(csvValue(report.getInstitutionName())).append(",");
            csv.append(csvValue(report.getSupplierName())).append(",");
            csv.append(csvValue(report.getContractDate())).append(",");
            csv.append(csvValue(report.getContractValueVat())).append(",");
            csv.append(csvValue(report.getCurrency())).append(",");
            csv.append(csvValue(report.getFinalRiskScore())).append(",");
            csv.append(csvValue(report.getRiskLevel())).append(",");
            csv.append(csvValue(report.getPriorityRank())).append(",");
            csv.append(csvValue(report.getExplanationText())).append("\n");
        }

        return csv.toString();
    }

    private ContractReportDto mapToContractReport(
            Contract contract,
            RiskAssessment riskAssessment,
            AIExplanation explanation
    ) {
        return ContractReportDto.builder()
                .contractId(contract.getId())
                .noticeNumber(contract.getNoticeNumber())
                .subject(contract.getSubject())
                .contractType(contract.getContractType())
                .procedureType(contract.getProcedureType())
                .contractDate(contract.getContractDate())
                .publicationDate(contract.getPublicationDate())
                .estimatedValueVat(contract.getEstimatedValueVat())
                .contractValueVat(contract.getContractValueVat())
                .currency(contract.getCurrency())
                .institutionName(contract.getInstitution() != null ? contract.getInstitution().getOfficialName() : null)
                .supplierName(contract.getSupplier() != null ? contract.getSupplier().getOfficialName() : null)

                .ruleScore(riskAssessment != null ? riskAssessment.getRuleScore() : null)
                .anomalyScore(riskAssessment != null ? riskAssessment.getAnomalyScore() : null)
                .similarityScore(riskAssessment != null ? riskAssessment.getSimilarityScore() : null)
                .clusterScore(riskAssessment != null ? riskAssessment.getClusterScore() : null)
                .finalRiskScore(riskAssessment != null ? riskAssessment.getFinalRiskScore() : null)
                .riskLevel(riskAssessment != null && riskAssessment.getRiskLevel() != null ? riskAssessment.getRiskLevel().name() : null)
                .priorityRank(riskAssessment != null ? riskAssessment.getPriorityRank() : null)
                .triggeredRiskFlags(mapRiskFlags(riskAssessment))

                .summaryText(explanation != null ? explanation.getSummaryText() : null)
                .explanationText(explanation != null ? explanation.getExplanationText() : null)
                .recommendationText(explanation != null ? explanation.getRecommendationText() : null)

                .lifecycleData(buildLifecycleData(contract, riskAssessment, explanation))
                .build();
    }

    private HighRiskContractReportDto mapToHighRiskReport(
            Contract contract,
            RiskAssessment riskAssessment,
            AIExplanation explanation
    ) {
        return HighRiskContractReportDto.builder()
                .contractId(contract.getId())
                .subject(contract.getSubject())
                .institutionName(contract.getInstitution() != null ? contract.getInstitution().getOfficialName() : null)
                .supplierName(contract.getSupplier() != null ? contract.getSupplier().getOfficialName() : null)
                .contractDate(contract.getContractDate())
                .contractValueVat(contract.getContractValueVat())
                .currency(contract.getCurrency())
                .finalRiskScore(riskAssessment.getFinalRiskScore())
                .riskLevel(riskAssessment.getRiskLevel() != null ? riskAssessment.getRiskLevel().name() : null)
                .priorityRank(riskAssessment.getPriorityRank())
                .triggeredRiskFlags(mapRiskFlags(riskAssessment))
                .explanationText(explanation != null ? explanation.getExplanationText() : null)
                .build();
    }

    private RiskAssessment findRiskAssessment(Contract contract) {
        if (contract.getRiskAssessment() != null) {
            return contract.getRiskAssessment();
        }

        return riskAssessmentRepository.findAll()
                .stream()
                .filter(riskAssessment -> riskAssessment.getContract() != null)
                .filter(riskAssessment -> Objects.equals(riskAssessment.getContract().getId(), contract.getId()))
                .findFirst()
                .orElse(null);
    }

    private AIExplanation findLatestExplanation(Long contractId) {
        return aiExplanationRepository.findAll()
                .stream()
                .filter(explanation -> explanation.getContract() != null)
                .filter(explanation -> Objects.equals(explanation.getContract().getId(), contractId))
                .max(Comparator.comparing(
                        AIExplanation::getGeneratedAt,
                        Comparator.nullsFirst(Comparator.naturalOrder())
                ))
                .orElse(null);
    }

    private boolean isHighRisk(RiskAssessment riskAssessment) {
        return riskAssessment.getRiskLevel() != null
                && riskAssessment.getRiskLevel().name().equalsIgnoreCase("HIGH");
    }

    private List<String> mapRiskFlags(RiskAssessment riskAssessment) {
        if (riskAssessment == null || riskAssessment.getTriggeredFlags() == null) {
            return List.of();
        }

        return riskAssessment.getTriggeredFlags()
                .stream()
                .map(this::formatRiskFlag)
                .toList();
    }

    private String formatRiskFlag(TriggeredRiskFlag flag) {
        if (flag.getFlagName() != null) {
            return flag.getFlagName();
        }

        return flag.getFlagCode();
    }

    private List<ReportLifecycleItemDto> buildLifecycleData(
            Contract contract,
            RiskAssessment riskAssessment,
            AIExplanation explanation
    ) {
        List<ReportLifecycleItemDto> lifecycle = new ArrayList<>();

        lifecycle.add(ReportLifecycleItemDto.builder()
                .phase("CONTRACT")
                .status("AVAILABLE")
                .date(contract.getContractDate())
                .description(contract.getSubject())
                .build());

        Decision decision = contract.getDecision();
        if (decision != null) {
            lifecycle.add(ReportLifecycleItemDto.builder()
                    .phase("DECISION")
                    .status("AVAILABLE")
                    .date(decision.getDecisionDate())
                    .description(decision.getDecisionText())
                    .build());
        }

        RealizedContract realizedContract = contract.getRealizedContract();
        if (realizedContract != null) {
            lifecycle.add(ReportLifecycleItemDto.builder()
                    .phase("REALIZED_CONTRACT")
                    .status("AVAILABLE")
                    .date(realizedContract.getPublicationDate())
                    .description(realizedContract.getSubject())
                    .build());
        }

        if (riskAssessment != null) {
            LocalDate evaluatedDate = riskAssessment.getEvaluatedAt() != null
                    ? riskAssessment.getEvaluatedAt().toLocalDate()
                    : null;

            lifecycle.add(ReportLifecycleItemDto.builder()
                    .phase("RISK_ASSESSMENT")
                    .status("AVAILABLE")
                    .date(evaluatedDate)
                    .description("Risk level: " + (riskAssessment.getRiskLevel() != null ? riskAssessment.getRiskLevel().name() : "N/A"))
                    .build());
        }

        if (explanation != null) {
            LocalDate generatedDate = explanation.getGeneratedAt() != null
                    ? explanation.getGeneratedAt().toLocalDate()
                    : null;

            lifecycle.add(ReportLifecycleItemDto.builder()
                    .phase("AI_EXPLANATION")
                    .status("AVAILABLE")
                    .date(generatedDate)
                    .description(explanation.getSummaryText())
                    .build());
        }

        return lifecycle;
    }

    private String csvValue(Object value) {
        return CsvExportUtil.escape(value == null ? null : value.toString());
    }
}