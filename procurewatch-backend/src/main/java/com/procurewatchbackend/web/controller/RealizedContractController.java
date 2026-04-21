package com.procurewatchbackend.web.controller;

import com.procurewatchbackend.dto.create.CreateRealizedContractDto;
import com.procurewatchbackend.dto.display.GetRealizedContractDto;
import com.procurewatchbackend.dto.edit.EditRealizedContractDto;
import com.procurewatchbackend.service.application.RealizedContractApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/realized-contracts")
@RequiredArgsConstructor
public class RealizedContractController {

    private final RealizedContractApplicationService realizedContractApplicationService;

    @PostMapping("/add")
    public ResponseEntity<GetRealizedContractDto> add(@RequestBody CreateRealizedContractDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(realizedContractApplicationService.add(dto));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<GetRealizedContractDto> edit(
            @PathVariable("id") Long id,
            @RequestBody EditRealizedContractDto dto
    ) {
        return ResponseEntity.ok(realizedContractApplicationService.edit(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        realizedContractApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getAllByInstitution/{institutionId}")
    public ResponseEntity<List<GetRealizedContractDto>> getAllByInstitution(
            @PathVariable("institutionId") Long institutionId
    ) {
        return ResponseEntity.ok(realizedContractApplicationService.getAllByInstitution(institutionId));
    }

    @GetMapping("/getAllBySupplier/{supplierId}")
    public ResponseEntity<List<GetRealizedContractDto>> getAllBySupplier(
            @PathVariable("supplierId") Long supplierId
    ) {
        return ResponseEntity.ok(realizedContractApplicationService.getAllBySupplier(supplierId));
    }

    @GetMapping("/getByContract/{contractId}")
    public ResponseEntity<GetRealizedContractDto> getByContract(@PathVariable("contractId") Long contractId) {
        return ResponseEntity.ok(realizedContractApplicationService.getByContract(contractId));
    }

    @GetMapping("/getByContractType/{contractType}")
    public ResponseEntity<List<GetRealizedContractDto>> getByContractType(
            @PathVariable("contractType") String contractType
    ) {
        return ResponseEntity.ok(realizedContractApplicationService.getByContractType(contractType));
    }

    @GetMapping("/getBySubject/{subject}")
    public ResponseEntity<List<GetRealizedContractDto>> getBySubject(@PathVariable("subject") String subject) {
        return ResponseEntity.ok(realizedContractApplicationService.getBySubject(subject));
    }

    @GetMapping("/getByProcedureType/{procedureType}")
    public ResponseEntity<List<GetRealizedContractDto>> getByProcedureType(
            @PathVariable("procedureType") String procedureType
    ) {
        return ResponseEntity.ok(realizedContractApplicationService.getByProcedureType(procedureType));
    }

    @GetMapping("/getByPublicationDate/{publicationDate}")
    public ResponseEntity<List<GetRealizedContractDto>> getByPublicationDate(
            @PathVariable("publicationDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publicationDate
    ) {
        return ResponseEntity.ok(realizedContractApplicationService.getByPublicationDate(publicationDate));
    }

    @GetMapping("/getByRepublishDate/{republishDate}")
    public ResponseEntity<List<GetRealizedContractDto>> getByRepublishDate(
            @PathVariable("republishDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate republishDate
    ) {
        return ResponseEntity.ok(realizedContractApplicationService.getByRepublishDate(republishDate));
    }

    @GetMapping("/getByAwardedValueVatGreaterThanEqual/{value}")
    public ResponseEntity<List<GetRealizedContractDto>> getByAwardedValueVatGreaterThanEqual(
            @PathVariable("value") BigDecimal value
    ) {
        return ResponseEntity.ok(realizedContractApplicationService.getByAwardedValueVatGreaterThanEqual(value));
    }

    @GetMapping("/getByAwardedValueVatLessThanEqual/{value}")
    public ResponseEntity<List<GetRealizedContractDto>> getByAwardedValueVatLessThanEqual(
            @PathVariable("value") BigDecimal value
    ) {
        return ResponseEntity.ok(realizedContractApplicationService.getByAwardedValueVatLessThanEqual(value));
    }

    @GetMapping("/getByAwardedValueVatBetween/{min}/{max}")
    public ResponseEntity<List<GetRealizedContractDto>> getByAwardedValueVatBetween(
            @PathVariable("min") BigDecimal min,
            @PathVariable("max") BigDecimal max
    ) {
        return ResponseEntity.ok(realizedContractApplicationService.getByAwardedValueVatBetween(min, max));
    }

    @GetMapping("/getByRealizedValueVatGreaterThanEqual/{value}")
    public ResponseEntity<List<GetRealizedContractDto>> getByRealizedValueVatGreaterThanEqual(
            @PathVariable("value") BigDecimal value
    ) {
        return ResponseEntity.ok(realizedContractApplicationService.getByRealizedValueVatGreaterThanEqual(value));
    }

    @GetMapping("/getByRealizedValueVatLessThanEqual/{value}")
    public ResponseEntity<List<GetRealizedContractDto>> getByRealizedValueVatLessThanEqual(
            @PathVariable("value") BigDecimal value
    ) {
        return ResponseEntity.ok(realizedContractApplicationService.getByRealizedValueVatLessThanEqual(value));
    }

    @GetMapping("/getByRealizedValueVatBetween/{min}/{max}")
    public ResponseEntity<List<GetRealizedContractDto>> getByRealizedValueVatBetween(
            @PathVariable("min") BigDecimal min,
            @PathVariable("max") BigDecimal max
    ) {
        return ResponseEntity.ok(realizedContractApplicationService.getByRealizedValueVatBetween(min, max));
    }

    @GetMapping("/getByPaidValueVatGreaterThanEqual/{value}")
    public ResponseEntity<List<GetRealizedContractDto>> getByPaidValueVatGreaterThanEqual(
            @PathVariable("value") BigDecimal value
    ) {
        return ResponseEntity.ok(realizedContractApplicationService.getByPaidValueVatGreaterThanEqual(value));
    }

    @GetMapping("/getByPaidValueVatLessThanEqual/{value}")
    public ResponseEntity<List<GetRealizedContractDto>> getByPaidValueVatLessThanEqual(
            @PathVariable("value") BigDecimal value
    ) {
        return ResponseEntity.ok(realizedContractApplicationService.getByPaidValueVatLessThanEqual(value));
    }

    @GetMapping("/getByPaidValueVatBetween/{min}/{max}")
    public ResponseEntity<List<GetRealizedContractDto>> getByPaidValueVatBetween(
            @PathVariable("min") BigDecimal min,
            @PathVariable("max") BigDecimal max
    ) {
        return ResponseEntity.ok(realizedContractApplicationService.getByPaidValueVatBetween(min, max));
    }

    @GetMapping("/allWhereRealizedValueExceedsAwardedValue")
    public ResponseEntity<List<GetRealizedContractDto>> allWhereRealizedValueExceedsAwardedValue() {
        return ResponseEntity.ok(realizedContractApplicationService.allWhereRealizedValueExceedsAwardedValue());
    }

    @GetMapping("/allWherePaidValueExceedsRealizedValue")
    public ResponseEntity<List<GetRealizedContractDto>> allWherePaidValueExceedsRealizedValue() {
        return ResponseEntity.ok(realizedContractApplicationService.allWherePaidValueExceedsRealizedValue());
    }

    @GetMapping("/allWherePaidValueIsLessThanOrEqualToRealizedValue")
    public ResponseEntity<List<GetRealizedContractDto>> allWherePaidValueIsLessThanOrEqualToRealizedValue() {
        return ResponseEntity.ok(realizedContractApplicationService.allWherePaidValueIsLessThanOrEqualToRealizedValue());
    }
}