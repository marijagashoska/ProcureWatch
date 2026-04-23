package com.procurewatchbackend.web.controller;

import com.procurewatchbackend.dto.display.GetContractDto;
import com.procurewatchbackend.dto.display.GetNoticeDto;
import com.procurewatchbackend.dto.display.GetPlanItemDto;
import com.procurewatchbackend.service.application.TextSimilarityApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/similarity")
@RequiredArgsConstructor
@Tag(name = "Text Similarity", description = "Text similarity and document matching endpoints")
public class TextSimilarityController {

    private final TextSimilarityApplicationService textSimilarityApplicationService;

    @PostMapping("/compare")
    @Operation(summary = "Compare two texts", description = "Returns similarity score between 0.0 and 1.0")
    public ResponseEntity<SimilarityResponse> compareTexts(
            @RequestParam String text1,
            @RequestParam String text2
    ) {
        double score = textSimilarityApplicationService.compareTexts(text1, text2);
        return ResponseEntity.ok(new SimilarityResponse(score));
    }

    @GetMapping("/contracts/{contractId}")
    @Operation(summary = "Find similar contracts", description = "Returns up to 5 similar contracts")
    public ResponseEntity<List<GetContractDto>> findSimilarContracts(
            @PathVariable Long contractId
    ) {
        List<GetContractDto> similar = textSimilarityApplicationService.findSimilarContracts(contractId);
        return ResponseEntity.ok(similar);
    }

    @GetMapping("/notices/{noticeId}")
    @Operation(summary = "Find similar notices", description = "Returns up to 5 similar notices")
    public ResponseEntity<List<GetNoticeDto>> findSimilarNotices(
            @PathVariable Long noticeId
    ) {
        List<GetNoticeDto> similar = textSimilarityApplicationService.findSimilarNotices(noticeId);
        return ResponseEntity.ok(similar);
    }

    @GetMapping("/plan-items/{planItemId}")
    @Operation(summary = "Find similar plan items", description = "Returns up to 5 similar plan items (filters by CPV code)")
    public ResponseEntity<List<GetPlanItemDto>> findSimilarPlanItems(
            @PathVariable Long planItemId
    ) {
        List<GetPlanItemDto> similar = textSimilarityApplicationService.findSimilarPlanItems(planItemId);
        return ResponseEntity.ok(similar);
    }

    @GetMapping("/documents/{contractId}")
    @Operation(summary = "Find all similar documents", description = "Returns similar contracts, notices, and plan items")
    public ResponseEntity<Map<String, Object>> findSimilarDocuments(
            @PathVariable Long contractId
    ) {
        Map<String, Object> similar = textSimilarityApplicationService.findSimilarDocuments(contractId);
        return ResponseEntity.ok(similar);
    }

    @GetMapping("/score/{contractId}")
    @Operation(summary = "Calculate contract similarity score", description = "Returns average similarity score (0.0-1.0)")
    public ResponseEntity<TextSimilarityApplicationService.SimilarityScoreDto> calculateContractSimilarityScore(
            @PathVariable Long contractId
    ) {
        TextSimilarityApplicationService.SimilarityScoreDto score = 
                textSimilarityApplicationService.calculateContractSimilarityScore(contractId);
        return ResponseEntity.ok(score);
    }

    public static class SimilarityResponse {
        public double score;
        public String percentage;

        public SimilarityResponse(double score) {
            this.score = score;
            this.percentage = String.format("%.2f%%", score * 100);
        }
    }
}
