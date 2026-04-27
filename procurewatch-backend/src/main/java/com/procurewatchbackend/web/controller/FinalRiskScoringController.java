package com.procurewatchbackend.web.controller;

import com.procurewatchbackend.dto.display.GetRiskAssessmentDto;
import com.procurewatchbackend.service.application.FinalRiskScoringApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/final-risk-scoring")
@RequiredArgsConstructor
public class FinalRiskScoringController {

    private final FinalRiskScoringApplicationService finalRiskScoringApplicationService;

    @PostMapping("/contract/{contractId}")
    public ResponseEntity<GetRiskAssessmentDto> recalculateContract(@PathVariable Long contractId) {
        return ResponseEntity.ok(finalRiskScoringApplicationService.recalculateContract(contractId));
    }

    @PostMapping("/all")
    public ResponseEntity<List<GetRiskAssessmentDto>> recalculateAll() {
        return ResponseEntity.ok(finalRiskScoringApplicationService.recalculateAll());
    }
}