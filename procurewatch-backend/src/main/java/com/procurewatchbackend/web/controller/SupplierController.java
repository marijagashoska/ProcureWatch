package com.procurewatchbackend.web.controller;

import com.procurewatchbackend.dto.create.CreateSupplierDto;
import com.procurewatchbackend.dto.display.GetSupplierDto;
import com.procurewatchbackend.dto.edit.EditSupplierDto;
import com.procurewatchbackend.service.application.SupplierApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierApplicationService supplierApplicationService;

    @PostMapping("/add")
    public ResponseEntity<GetSupplierDto> add(@RequestBody CreateSupplierDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierApplicationService.add(dto));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<GetSupplierDto> edit(
            @PathVariable("id") Long id,
            @RequestBody EditSupplierDto dto
    ) {
        return ResponseEntity.ok(supplierApplicationService.edit(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        supplierApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<GetSupplierDto>> getAll() {
        return ResponseEntity.ok(supplierApplicationService.getAll());
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<GetSupplierDto> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(supplierApplicationService.getById(id));
    }

    @GetMapping("/getByOfficialName/{officialName}")
    public ResponseEntity<List<GetSupplierDto>> getByOfficialName(
            @PathVariable("officialName") String officialName
    ) {
        return ResponseEntity.ok(supplierApplicationService.getByOfficialName(officialName));
    }

    @GetMapping("/getByNormalizedName/{normalizedName}")
    public ResponseEntity<List<GetSupplierDto>> getByNormalizedName(
            @PathVariable("normalizedName") String normalizedName
    ) {
        return ResponseEntity.ok(supplierApplicationService.getByNormalizedName(normalizedName));
    }

    @GetMapping("/getByExternalId/{externalId}")
    public ResponseEntity<GetSupplierDto> getByExternalId(@PathVariable("externalId") String externalId) {
        return ResponseEntity.ok(supplierApplicationService.getByExternalId(externalId));
    }

    @GetMapping("/getByRealOwnersInfo/{realOwnersInfo}")
    public ResponseEntity<List<GetSupplierDto>> getByRealOwnersInfo(
            @PathVariable("realOwnersInfo") String realOwnersInfo
    ) {
        return ResponseEntity.ok(supplierApplicationService.getByRealOwnersInfo(realOwnersInfo));
    }

    @GetMapping("/allThatHaveDecision")
    public ResponseEntity<List<GetSupplierDto>> allThatHaveDecision() {
        return ResponseEntity.ok(supplierApplicationService.allThatHaveDecision());
    }

    @GetMapping("/allThatDontHaveDecision")
    public ResponseEntity<List<GetSupplierDto>> allThatDontHaveDecision() {
        return ResponseEntity.ok(supplierApplicationService.allThatDontHaveDecision());
    }
}