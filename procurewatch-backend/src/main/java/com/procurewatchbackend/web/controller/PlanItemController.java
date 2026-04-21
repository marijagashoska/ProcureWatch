package com.procurewatchbackend.web.controller;

import com.procurewatchbackend.dto.create.CreatePlanItemDto;
import com.procurewatchbackend.dto.display.GetPlanItemDto;
import com.procurewatchbackend.dto.edit.EditPlanItemDto;
import com.procurewatchbackend.service.application.PlanItemApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plan-items")
@RequiredArgsConstructor
public class PlanItemController {

    private final PlanItemApplicationService planItemApplicationService;

    @PostMapping("/add")
    public ResponseEntity<GetPlanItemDto> add(@RequestBody CreatePlanItemDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(planItemApplicationService.add(dto));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<GetPlanItemDto> edit(
            @PathVariable("id") Long id,
            @RequestBody EditPlanItemDto dto
    ) {
        return ResponseEntity.ok(planItemApplicationService.edit(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        planItemApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<GetPlanItemDto>> getAll() {
        return ResponseEntity.ok(planItemApplicationService.getAll());
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<GetPlanItemDto> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(planItemApplicationService.getById(id));
    }

    @GetMapping("/getByPlanId/{planId}")
    public ResponseEntity<List<GetPlanItemDto>> getByPlanId(@PathVariable("planId") Long planId) {
        return ResponseEntity.ok(planItemApplicationService.getByPlanId(planId));
    }
}