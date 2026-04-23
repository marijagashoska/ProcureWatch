package com.procurewatchbackend.web.controller;

import com.procurewatchbackend.dto.display.GetRiskAssessmentDto;
import com.procurewatchbackend.model.enums.RiskLevel;
import com.procurewatchbackend.service.application.RiskAssessmentApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk-assessments")
@RequiredArgsConstructor
public class RiskAssessmentController {

    private final RiskAssessmentApplicationService riskAssessmentApplicationService;

    @PostMapping("/evaluate/contract/{contractId}")
    public ResponseEntity<GetRiskAssessmentDto> evaluateContract(@PathVariable Long contractId) {
        return ResponseEntity.ok(riskAssessmentApplicationService.evaluateContract(contractId));
    }

    @PostMapping("/evaluate/all")
    public ResponseEntity<List<GetRiskAssessmentDto>> evaluateAllContracts() {
        return ResponseEntity.ok(riskAssessmentApplicationService.evaluateAllContracts());
    }

    @GetMapping("/contract/{contractId}")
    public ResponseEntity<GetRiskAssessmentDto> getByContractId(@PathVariable Long contractId) {
        return ResponseEntity.ok(riskAssessmentApplicationService.getByContractId(contractId));
    }

    @GetMapping("/risk-level/{riskLevel}")
    public ResponseEntity<List<GetRiskAssessmentDto>> getByRiskLevel(@PathVariable RiskLevel riskLevel) {
        return ResponseEntity.ok(riskAssessmentApplicationService.getByRiskLevel(riskLevel));
    }
}