package com.procurewatchbackend.ai;

import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.model.entity.RiskAssessment;
import com.procurewatchbackend.repository.ContractRepository;
import com.procurewatchbackend.repository.RiskAssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final ContractRepository contractRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final AnomalyDetectionService anomalyService;
    private final ExternalAiService externalAiService;

    @GetMapping("/analyze/{id}")
    public ResponseEntity<AiRiskResponse> analyze(@PathVariable Long id) {
        // 1. Повлечи податоци
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Договорот не е најден"));

        // 2. Пресметај аномалија
        double score = anomalyService.calculateAnomaly(contract);

        // 3. Генерирај текстови
        String summary = externalAiService.generateSummary(score);
        String explanation = externalAiService.generateExplanation(score);
        String recommendation = externalAiService.generateRecommendation(score);

        // 4. ЗАЧУВАЈ ВО БАЗА
        RiskAssessment assessment = riskAssessmentRepository.findByContractId(id)
                .orElse(new RiskAssessment());

        assessment.setContract(contract);
        assessment.setAnomalyScore(BigDecimal.valueOf(score * 100)); // anomalyScore
        // Овде колегата од Модул 9 ќе го пополни финалниот RiskScore

        riskAssessmentRepository.save(assessment);

        // 5. Врати одговор за другите модули
        return ResponseEntity.ok(new AiRiskResponse(
                id,
                score * 100,
                score > 0.7 ? "HIGH" : "LOW",
                summary,
                explanation,
                recommendation,
                List.of(score > 0.7 ? "OUTLIER_DETECTED" : "NORMAL_DATA")
        ));
    }
}