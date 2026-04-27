package com.procurewatchbackend.web.controller;

import com.procurewatchbackend.dto.display.detail.GetContractDetailDto;
import com.procurewatchbackend.service.application.ContractDetailApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractDetailController {

    private final ContractDetailApplicationService contractDetailApplicationService;

    @GetMapping("/{contractId}/detail")
    public ResponseEntity<GetContractDetailDto> getContractDetail(
            @PathVariable Long contractId,
            @RequestParam(defaultValue = "false") boolean evaluateRiskIfMissing,
            @RequestParam(defaultValue = "true") boolean includeSimilar
    ) {
        return ResponseEntity.ok(
                contractDetailApplicationService.getContractDetail(
                        contractId,
                        evaluateRiskIfMissing,
                        includeSimilar
                )
        );
    }
}