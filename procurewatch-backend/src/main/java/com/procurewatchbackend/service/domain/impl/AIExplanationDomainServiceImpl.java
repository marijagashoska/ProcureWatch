package com.procurewatchbackend.service.domain.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.procurewatchbackend.dto.ai.GeneratedExplanationDto;
import com.procurewatchbackend.model.entity.AIExplanation;
import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.model.entity.Decision;
import com.procurewatchbackend.model.entity.RealizedContract;
import com.procurewatchbackend.model.entity.RiskAssessment;
import com.procurewatchbackend.model.entity.TriggeredRiskFlag;
import com.procurewatchbackend.repository.AIExplanationRepository;
import com.procurewatchbackend.repository.ContractRepository;
import com.procurewatchbackend.repository.RiskAssessmentRepository;
import com.procurewatchbackend.service.domain.AIExplanationDomainService;
import com.procurewatchbackend.service.domain.RiskAssessmentDomainService;
import com.procurewatchbackend.service.integration.GroqExplanationClient;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AIExplanationDomainServiceImpl implements AIExplanationDomainService {

    private static final String GENERATOR_TYPE_GROQ = "GROQ_AI";
    private static final String GENERATOR_TYPE_FALLBACK = "TEMPLATE_FALLBACK";

    private final ContractRepository contractRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final RiskAssessmentDomainService riskAssessmentDomainService;
    private final AIExplanationRepository aiExplanationRepository;
    private final GroqExplanationClient groqExplanationClient;

    @Override
    public AIExplanation generateForContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found with id: " + contractId));

        RiskAssessment assessment = riskAssessmentRepository.findByContractId(contractId)
                .orElseGet(() -> riskAssessmentDomainService.evaluateContract(contractId));

        GeneratedExplanationDto generated;
        String generatorType;

        try {
            String prompt = buildPrompt(contract, assessment);
            generated = groqExplanationClient.generateExplanation(prompt);
            generatorType = GENERATOR_TYPE_GROQ;
        } catch (Exception ex) {
            generated = buildFallbackExplanation(contract, assessment, ex.getMessage());
            generatorType = GENERATOR_TYPE_FALLBACK;
        }

        AIExplanation explanation = AIExplanation.builder()
                .contract(contract)
                .riskAssessment(assessment)
                .summaryText(generated.summaryText())
                .explanationText(generated.explanationText())
                .recommendationText(generated.recommendationText())
                .generatorType(generatorType)
                .modelVersion(generatorType.equals(GENERATOR_TYPE_GROQ)
                        ? "llama-3.3-70b-versatile"
                        : "template-fallback-v1")
                .generatedAt(LocalDateTime.now())
                .build();

        return aiExplanationRepository.save(explanation);
    }

    @Override
    @Transactional(readOnly = true)
    public AIExplanation getLatestByContractId(Long contractId) {
        return aiExplanationRepository.findTopByContractIdOrderByGeneratedAtDesc(contractId)
                .orElseThrow(() -> new EntityNotFoundException("No explanation found for contract id: " + contractId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AIExplanation> getHistoryByContractId(Long contractId) {
        return aiExplanationRepository.findByContractIdOrderByGeneratedAtDesc(contractId);
    }

    private String buildPrompt(Contract contract, RiskAssessment assessment) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                Generate a public procurement risk explanation.

                Return only valid JSON in this exact shape:
                {
                  "summaryText": "...",
                  "explanationText": "...",
                  "recommendationText": "..."
                }

                Accuracy rules:
                - Use only the provided data.
                - Do not invent facts, causes, corruption, fraud, intent, or legal conclusions.
                - Do not say "anomaly" unless anomalyScore is provided or an anomaly-related triggered flag exists.
                - Do not say a delay is "unusually long" unless a delay-related triggered flag is provided.
                - Do not say there is a discrepancy unless the exact compared values are provided.
                - If data is missing, say it is missing and should be verified.
                - Explain risk as "risk indicators", not proof of wrongdoing.
                - Mention that risk indicators require analyst/auditor review.
                - Keep summaryText short and evidence-based.
                - Keep explanationText detailed but concise.
                - Keep recommendationText action-oriented and neutral.
                - Write for an analyst/auditor.

                Data:
                """);

        prompt.append("\nContract ID: ").append(contract.getId());
        prompt.append("\nNotice number: ").append(nullSafe(contract.getNoticeNumber()));
        prompt.append("\nSubject: ").append(nullSafe(contract.getSubject()));
        prompt.append("\nContract type: ").append(nullSafe(contract.getContractType()));
        prompt.append("\nProcedure type: ").append(nullSafe(contract.getProcedureType()));
        prompt.append("\nContract date: ").append(contract.getContractDate());
        prompt.append("\nPublication date: ").append(contract.getPublicationDate());
        prompt.append("\nEstimated value VAT: ").append(decimalSafe(contract.getEstimatedValueVat()));
        prompt.append("\nContract value VAT: ").append(decimalSafe(contract.getContractValueVat()));
        prompt.append("\nCurrency: ").append(nullSafe(contract.getCurrency()));

        if (contract.getInstitution() != null) {
            prompt.append("\nInstitution: ").append(nullSafe(contract.getInstitution().getOfficialName()));
            prompt.append("\nInstitution city: ").append(nullSafe(contract.getInstitution().getCity()));
            prompt.append("\nInstitution category: ").append(nullSafe(contract.getInstitution().getCategory()));
        } else {
            prompt.append("\nInstitution: N/A");
        }

        if (contract.getSupplier() != null) {
            prompt.append("\nSupplier: ").append(nullSafe(contract.getSupplier().getOfficialName()));
        } else {
            prompt.append("\nSupplier: N/A");
        }

        prompt.append("\n\nRisk assessment:");
        prompt.append("\nRule score: ").append(decimalSafe(assessment.getRuleScore()));
        prompt.append("\nAnomaly score: ").append(decimalSafe(assessment.getAnomalyScore()));
        prompt.append("\nSimilarity score: ").append(decimalSafe(assessment.getSimilarityScore()));
        prompt.append("\nCluster score: ").append(decimalSafe(assessment.getClusterScore()));
        prompt.append("\nFinal risk score: ").append(decimalSafe(assessment.getFinalRiskScore()));
        prompt.append("\nRisk level: ").append(assessment.getRiskLevel());
        prompt.append("\nPriority rank: ").append(assessment.getPriorityRank());
        prompt.append("\nRisk model version: ").append(nullSafe(assessment.getModelVersion()));

        appendFlags(prompt, assessment.getTriggeredFlags());
        appendDecision(prompt, contract.getDecision());
        appendRealizedContract(prompt, contract.getRealizedContract());

        return prompt.toString();
    }

    private void appendFlags(StringBuilder prompt, List<TriggeredRiskFlag> flags) {
        prompt.append("\n\nTriggered risk flags:");

        if (flags == null || flags.isEmpty()) {
            prompt.append("\nNone");
            return;
        }

        for (TriggeredRiskFlag flag : flags) {
            prompt.append("\n- Code: ").append(nullSafe(flag.getFlagCode()));
            prompt.append(", Name: ").append(nullSafe(flag.getFlagName()));
            prompt.append(", Description: ").append(nullSafe(flag.getFlagDescription()));
            prompt.append(", Weight: ").append(decimalSafe(flag.getWeight()));
            prompt.append(", Measured value: ").append(nullSafe(flag.getMeasuredValue()));
            prompt.append(", Threshold value: ").append(nullSafe(flag.getThresholdValue()));
        }
    }

    private void appendDecision(StringBuilder prompt, Decision decision) {
        prompt.append("\n\nDecision:");

        if (decision == null) {
            prompt.append("\nNo linked decision record found.");
            return;
        }

        prompt.append("\nDecision date: ").append(decision.getDecisionDate());
        prompt.append("\nDecision subject: ").append(nullSafe(decision.getSubject()));
        prompt.append("\nDecision text: ").append(nullSafe(decision.getDecisionText()));
        prompt.append("\nDecision procedure type: ").append(nullSafe(decision.getProcedureType()));
    }

    private void appendRealizedContract(StringBuilder prompt, RealizedContract realizedContract) {
        prompt.append("\n\nRealized contract:");

        if (realizedContract == null) {
            prompt.append("\nNo linked realized contract record found.");
            return;
        }

        prompt.append("\nAwarded value VAT: ").append(decimalSafe(realizedContract.getAwardedValueVat()));
        prompt.append("\nRealized value VAT: ").append(decimalSafe(realizedContract.getRealizedValueVat()));
        prompt.append("\nPaid value VAT: ").append(decimalSafe(realizedContract.getPaidValueVat()));
        prompt.append("\nPublication date: ").append(realizedContract.getPublicationDate());
        prompt.append("\nRepublish date: ").append(realizedContract.getRepublishDate());
    }

    private GeneratedExplanationDto buildFallbackExplanation(
            Contract contract,
            RiskAssessment assessment,
            String errorMessage
    ) {
        String summary = "Contract '" + nullSafe(contract.getSubject()) + "' has risk level "
                + assessment.getRiskLevel() + " with final risk score "
                + decimalSafe(assessment.getFinalRiskScore()) + ".";

        String explanation = "The external Groq AI API could not generate an explanation, so a fallback explanation was created. "
                + "Based on the available data, the contract has rule score " + decimalSafe(assessment.getRuleScore())
                + ", similarity score " + decimalSafe(assessment.getSimilarityScore())
                + ", anomaly score " + decimalSafe(assessment.getAnomalyScore())
                + ", and final risk score " + decimalSafe(assessment.getFinalRiskScore()) + ". "
                + "Triggered risk indicators count: "
                + (assessment.getTriggeredFlags() == null ? 0 : assessment.getTriggeredFlags().size())
                + ". These indicators do not prove wrongdoing, but they show that the contract should be reviewed. "
                + "API error: " + nullSafe(errorMessage);

        String recommendation;

        if (assessment.getRiskLevel() == null) {
            recommendation = "Review this contract manually because no risk level is available.";
        } else {
            recommendation = switch (assessment.getRiskLevel()) {
                case LOW -> "No urgent action is required. Keep the contract in standard monitoring.";
                case MEDIUM -> "Review the triggered indicators and compare the contract with similar procurements.";
                case HIGH -> "Prioritize this contract for analyst review and verify the lifecycle records.";
                case CRITICAL -> "Immediate audit review is recommended. Verify procurement documentation, value changes, supplier data, realization data, and payment records.";
            };
        }

        return new GeneratedExplanationDto(summary, explanation, recommendation);
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }

    private String decimalSafe(BigDecimal value) {
        return value == null ? "N/A" : value.stripTrailingZeros().toPlainString();
    }
}