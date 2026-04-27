package com.procurewatchbackend.service.application.impl;

import com.procurewatchbackend.dto.display.GetContractDto;
import com.procurewatchbackend.dto.display.GetContractLifecycleDto;
import com.procurewatchbackend.dto.display.GetInstitutionDto;
import com.procurewatchbackend.dto.display.GetRealizedContractDto;
import com.procurewatchbackend.dto.display.GetRiskAssessmentDto;
import com.procurewatchbackend.dto.display.GetSupplierDto;
import com.procurewatchbackend.dto.display.GetTriggeredRiskFlagDto;
import com.procurewatchbackend.dto.display.detail.ContractDetailExplanationDto;
import com.procurewatchbackend.dto.display.detail.GetContractDetailDto;
import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.model.entity.Decision;
import com.procurewatchbackend.model.entity.Institution;
import com.procurewatchbackend.model.entity.RealizedContract;
import com.procurewatchbackend.model.entity.RiskAssessment;
import com.procurewatchbackend.model.entity.Supplier;
import com.procurewatchbackend.model.entity.TriggeredRiskFlag;
import com.procurewatchbackend.repository.ContractRepository;
import com.procurewatchbackend.repository.RiskAssessmentRepository;
import com.procurewatchbackend.service.application.ContractDetailApplicationService;
import com.procurewatchbackend.service.application.LifecycleApplicationService;
import com.procurewatchbackend.service.application.TextSimilarityApplicationService;
import com.procurewatchbackend.service.domain.RiskAssessmentDomainService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContractDetailApplicationServiceImpl implements ContractDetailApplicationService {

    private final ContractRepository contractRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final LifecycleApplicationService lifecycleApplicationService;
    private final RiskAssessmentDomainService riskAssessmentDomainService;
    private final TextSimilarityApplicationService textSimilarityApplicationService;

    @Override
    @Transactional
    public GetContractDetailDto getContractDetail(
            Long contractId,
            boolean evaluateRiskIfMissing,
            boolean includeSimilar
    ) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Contract not found with id: " + contractId
                ));

        RiskAssessment assessment = riskAssessmentRepository.findByContractId(contractId)
                .orElse(null);

        if (assessment == null && evaluateRiskIfMissing) {
            assessment = riskAssessmentDomainService.evaluateContract(contractId);
        }

        GetContractLifecycleDto lifecycle = lifecycleApplicationService.getByContractId(contractId);

        Map<String, Object> similarDocuments = includeSimilar
                ? textSimilarityApplicationService.findSimilarDocuments(contractId)
                : Map.of();

        return new GetContractDetailDto(
                contract.getId(),
                contract.getSubject(),
                mapContract(contract),
                mapInstitution(contract.getInstitution()),
                mapSupplier(contract.getSupplier()),
                lifecycle,
                assessment != null ? mapRiskAssessment(assessment) : null,
                buildExplanation(contract, assessment),
                mapRealizedContract(contract.getRealizedContract()),
                similarDocuments
        );
    }

    private GetContractDto mapContract(Contract contract) {
        return new GetContractDto(
                contract.getId(),
                contract.getInstitution() != null ? contract.getInstitution().getId() : null,
                contract.getSupplier() != null ? contract.getSupplier().getId() : null,
                contract.getDecision() != null ? contract.getDecision().getId() : null,
                contract.getRealizedContract() != null ? contract.getRealizedContract().getId() : null,
                contract.getNoticeNumber(),
                contract.getSubject(),
                contract.getContractType(),
                contract.getProcedureType(),
                contract.getContractDate(),
                contract.getPublicationDate(),
                contract.getEstimatedValueVat(),
                contract.getContractValueVat(),
                contract.getCurrency(),
                contract.getSourceUrl()
        );
    }

    private GetInstitutionDto mapInstitution(Institution institution) {
        if (institution == null) {
            return null;
        }

        return new GetInstitutionDto(
                institution.getId(),
                institution.getExternalId(),
                institution.getOfficialName(),
                institution.getNormalizedName(),
                institution.getInstitutionType(),
                institution.getCity(),
                institution.getPostalCode(),
                institution.getCategory(),
                institution.getSourceUrl()
        );
    }

    private GetSupplierDto mapSupplier(Supplier supplier) {
        if (supplier == null) {
            return null;
        }

        List<Long> decisionIds = supplier.getDecisions() == null
                ? List.of()
                : supplier.getDecisions()
                .stream()
                .map(Decision::getId)
                .toList();

        return new GetSupplierDto(
                supplier.getId(),
                supplier.getExternalId(),
                supplier.getOfficialName(),
                supplier.getNormalizedName(),
                supplier.getRealOwnersInfo(),
                supplier.getSourceUrl(),
                decisionIds
        );
    }

    private GetRealizedContractDto mapRealizedContract(RealizedContract realizedContract) {
        if (realizedContract == null) {
            return null;
        }

        return new GetRealizedContractDto(
                realizedContract.getId(),
                realizedContract.getInstitution() != null ? realizedContract.getInstitution().getId() : null,
                realizedContract.getSupplier() != null ? realizedContract.getSupplier().getId() : null,
                realizedContract.getContract() != null ? realizedContract.getContract().getId() : null,
                realizedContract.getNoticeNumber(),
                realizedContract.getSubject(),
                realizedContract.getContractType(),
                realizedContract.getProcedureType(),
                realizedContract.getAwardedValueVat(),
                realizedContract.getRealizedValueVat(),
                realizedContract.getPaidValueVat(),
                realizedContract.getPublicationDate(),
                realizedContract.getRepublishDate(),
                realizedContract.getSourceUrl()
        );
    }

    private GetRiskAssessmentDto mapRiskAssessment(RiskAssessment assessment) {
        return new GetRiskAssessmentDto(
                assessment.getId(),
                assessment.getContract() != null ? assessment.getContract().getId() : null,
                assessment.getRuleScore(),
                assessment.getAnomalyScore(),
                assessment.getSimilarityScore(),
                assessment.getClusterScore(),
                assessment.getFinalRiskScore(),
                assessment.getRiskLevel(),
                assessment.getPriorityRank(),
                assessment.getModelVersion(),
                assessment.getEvaluatedAt(),
                assessment.getTriggeredFlags() == null
                        ? List.of()
                        : assessment.getTriggeredFlags()
                        .stream()
                        .map(this::mapTriggeredFlag)
                        .toList()
        );
    }

    private GetTriggeredRiskFlagDto mapTriggeredFlag(TriggeredRiskFlag flag) {
        return new GetTriggeredRiskFlagDto(
                flag.getId(),
                flag.getFlagCode(),
                flag.getFlagName(),
                flag.getFlagDescription(),
                flag.getWeight(),
                flag.getMeasuredValue(),
                flag.getThresholdValue(),
                flag.getCreatedAt()
        );
    }

    private ContractDetailExplanationDto buildExplanation(
            Contract contract,
            RiskAssessment assessment
    ) {
        if (assessment == null) {
            return new ContractDetailExplanationDto(
                    "Risk analysis has not been generated yet.",
                    "This contract does not have a saved RiskAssessment record. Run risk analysis to calculate rule score, triggered flags, final risk score, risk level and priority rank.",
                    "Run risk analysis for this contract before reviewing it."
            );
        }

        List<TriggeredRiskFlag> flags = assessment.getTriggeredFlags() == null
                ? List.of()
                : assessment.getTriggeredFlags();

        String riskLevel = assessment.getRiskLevel() == null
                ? "UNKNOWN"
                : assessment.getRiskLevel().name();

        String score = assessment.getFinalRiskScore() == null
                ? "not calculated"
                : assessment.getFinalRiskScore().toPlainString();

        String summary = "This contract is classified as " + riskLevel
                + " risk with final risk score " + score + ".";

        String explanation;

        if (flags.isEmpty()) {
            explanation = "No rule-based risk flags were triggered for this contract.";
        } else {
            String flagNames = flags.stream()
                    .map(TriggeredRiskFlag::getFlagName)
                    .toList()
                    .toString();

            explanation = "The contract was marked as risky because the following indicators were triggered: "
                    + flagNames + ".";
        }

        String recommendation = switch (riskLevel) {
            case "CRITICAL" -> "Immediate auditor review is recommended. Verify documentation, supplier history, lifecycle completeness and payment/realization records.";
            case "HIGH" -> "Auditor review is recommended. Check the triggered flags and compare this contract with similar cases.";
            case "MEDIUM" -> "Manual review may be needed if the institution, supplier or value is important.";
            case "LOW" -> "No urgent action is required, but the contract can remain available for regular monitoring.";
            default -> "Run or refresh the risk analysis to get a clearer recommendation.";
        };

        return new ContractDetailExplanationDto(
                summary,
                explanation,
                recommendation
        );
    }
}