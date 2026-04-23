package com.procurewatchbackend.web.controller;

import com.procurewatchbackend.dto.create.CreateProcurementPlanDto;
import com.procurewatchbackend.dto.display.GetPlanItemDto;
import com.procurewatchbackend.dto.display.GetProcurementPlanDto;
import com.procurewatchbackend.dto.display.PagedResponseDto;
import com.procurewatchbackend.dto.edit.EditProcurementPlanDto;
import com.procurewatchbackend.service.application.ProcurementPlanApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procurement-plans")
@RequiredArgsConstructor
public class ProcurementPlanController {

    private final ProcurementPlanApplicationService procurementPlanApplicationService;

    @PostMapping("/add")
    public ResponseEntity<GetProcurementPlanDto> add(@RequestBody CreateProcurementPlanDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(procurementPlanApplicationService.add(dto));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<GetProcurementPlanDto> edit(
            @PathVariable("id") Long id,
            @RequestBody EditProcurementPlanDto dto
    ) {
        return ResponseEntity.ok(procurementPlanApplicationService.edit(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        procurementPlanApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<GetProcurementPlanDto>> getAll() {
        return ResponseEntity.ok(procurementPlanApplicationService.getAll());
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<GetProcurementPlanDto> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(procurementPlanApplicationService.getById(id));
    }

    @GetMapping("/getByInstitutionId/{institutionId}")
    public ResponseEntity<List<GetProcurementPlanDto>> getByInstitutionId(
            @PathVariable("institutionId") Long institutionId
    ) {
        return ResponseEntity.ok(procurementPlanApplicationService.getByInstitutionId(institutionId));
    }

    @GetMapping("/getByYear/{year}")
    public ResponseEntity<List<GetProcurementPlanDto>> getByYear(@PathVariable("year") Integer year) {
        return ResponseEntity.ok(procurementPlanApplicationService.getByYear(year));
    }
    @GetMapping("/paged")
    public ResponseEntity<PagedResponseDto<GetProcurementPlanDto>> getAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(
                procurementPlanApplicationService.getAllPaginated(page, size, sortBy, sortDir)
        );
    }
}