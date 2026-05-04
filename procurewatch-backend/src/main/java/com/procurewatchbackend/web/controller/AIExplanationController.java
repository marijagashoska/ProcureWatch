package com.procurewatchbackend.web.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.procurewatchbackend.dto.display.GetAIExplanationDto;
import com.procurewatchbackend.service.application.AIExplanationApplicationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/explanations")
@RequiredArgsConstructor
public class AIExplanationController {

    private final AIExplanationApplicationService aiExplanationApplicationService;

    @PostMapping("/generate/contract/{contractId}")
    public ResponseEntity<GetAIExplanationDto> generateForContract(@PathVariable Long contractId) {
        return ResponseEntity.ok(aiExplanationApplicationService.generateForContract(contractId));
    }

    @GetMapping("/contract/{contractId}/latest")
    public ResponseEntity<GetAIExplanationDto> getLatestByContractId(@PathVariable Long contractId) {
        return ResponseEntity.ok(aiExplanationApplicationService.getLatestByContractId(contractId));
    }

    @GetMapping("/contract/{contractId}/history")
    public ResponseEntity<List<GetAIExplanationDto>> getHistoryByContractId(@PathVariable Long contractId) {
        return ResponseEntity.ok(aiExplanationApplicationService.getHistoryByContractId(contractId));
    }
}