package com.procurewatchbackend.web.controller;

import com.procurewatchbackend.dto.display.GetContractLifecycleDto;
import com.procurewatchbackend.service.application.LifecycleApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lifecycle")
@RequiredArgsConstructor
public class LifecycleController {

    private final LifecycleApplicationService lifecycleApplicationService;

    @GetMapping("/contract/{contractId}")
    public ResponseEntity<GetContractLifecycleDto> getByContractId(@PathVariable Long contractId) {
        return ResponseEntity.ok(lifecycleApplicationService.getByContractId(contractId));
    }
}