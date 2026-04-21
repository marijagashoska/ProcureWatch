package com.procurewatchbackend.web.controller;

import com.procurewatchbackend.dto.create.CreateDecisionDto;
import com.procurewatchbackend.dto.display.GetDecisionDto;
import com.procurewatchbackend.dto.edit.EditDecisionDto;
import com.procurewatchbackend.service.application.DecisionApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/decisions")
@RequiredArgsConstructor
public class DecisionController {

    private final DecisionApplicationService decisionApplicationService;

    @PostMapping("/add")
    public ResponseEntity<GetDecisionDto> add(@RequestBody CreateDecisionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(decisionApplicationService.add(dto));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<GetDecisionDto> edit(
            @PathVariable("id") Long id,
            @RequestBody EditDecisionDto dto
    ) {
        return ResponseEntity.ok(decisionApplicationService.edit(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        decisionApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getAllForInstitution/{institutionId}")
    public ResponseEntity<List<GetDecisionDto>> getAllForInstitution(
            @PathVariable("institutionId") Long institutionId
    ) {
        return ResponseEntity.ok(decisionApplicationService.getAllForInstitution(institutionId));
    }

    @GetMapping("/getAllForSupplier/{supplierId}")
    public ResponseEntity<List<GetDecisionDto>> getAllForSupplier(
            @PathVariable("supplierId") Long supplierId
    ) {
        return ResponseEntity.ok(decisionApplicationService.getAllForSuplier(supplierId));
    }

    @GetMapping("/getAllByProcedureType/{procedureType}")
    public ResponseEntity<List<GetDecisionDto>> getAllByProcedureType(
            @PathVariable("procedureType") String procedureType
    ) {
        return ResponseEntity.ok(decisionApplicationService.getAllByProcedureType(procedureType));
    }

    @GetMapping("/getAllBySubject/{subject}")
    public ResponseEntity<List<GetDecisionDto>> getAllBySubject(@PathVariable("subject") String subject) {
        return ResponseEntity.ok(decisionApplicationService.getAllBySubject(subject));
    }

    @GetMapping("/getAllByDecisionDate/{decisionDate}")
    public ResponseEntity<List<GetDecisionDto>> getAllByDecisionDate(
            @PathVariable("decisionDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate decisionDate
    ) {
        return ResponseEntity.ok(decisionApplicationService.getAllByDecisionDate(decisionDate));
    }

    @GetMapping("/getDecisionByNotice/{noticeId}")
    public ResponseEntity<List<GetDecisionDto>> getDecisionByNotice(
            @PathVariable("noticeId") Long noticeId
    ) {
        return ResponseEntity.ok(decisionApplicationService.getAllByNotice(noticeId));
    }
}