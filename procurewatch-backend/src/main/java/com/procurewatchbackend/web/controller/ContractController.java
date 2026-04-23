package com.procurewatchbackend.web.controller;

import com.procurewatchbackend.dto.create.CreateContractDto;
import com.procurewatchbackend.dto.display.GetContractDto;
import com.procurewatchbackend.dto.display.GetContractTableRowDto;
import com.procurewatchbackend.dto.display.GetInstitutionDto;
import com.procurewatchbackend.dto.display.PagedResponseDto;
import com.procurewatchbackend.dto.edit.EditContractDto;
import com.procurewatchbackend.model.enums.RiskLevel;
import com.procurewatchbackend.service.application.ContractApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractApplicationService contractApplicationService;

    @PostMapping("/add")
    public ResponseEntity<GetContractDto> add(@RequestBody CreateContractDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contractApplicationService.add(dto));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<GetContractDto> edit(
            @PathVariable("id") Long id,
            @RequestBody EditContractDto dto
    ) {
        return ResponseEntity.ok(contractApplicationService.edit(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        contractApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getAllByInstitution/{institutionId}")
    public ResponseEntity<List<GetContractDto>> getAllByInstitution(
            @PathVariable("institutionId") Long institutionId
    ) {
        return ResponseEntity.ok(contractApplicationService.getAllByInstitution(institutionId));
    }

    @GetMapping("/getAllBySupplier/{supplierId}")
    public ResponseEntity<List<GetContractDto>> getAllBySupplier(
            @PathVariable("supplierId") Long supplierId
    ) {
        return ResponseEntity.ok(contractApplicationService.getAllBySupplier(supplierId));
    }

    @GetMapping("/getByContractType/{contractType}")
    public ResponseEntity<List<GetContractDto>> getByContractType(
            @PathVariable("contractType") String contractType
    ) {
        return ResponseEntity.ok(contractApplicationService.getByContractType(contractType));
    }

    @GetMapping("/getBySubject/{subject}")
    public ResponseEntity<List<GetContractDto>> getBySubject(@PathVariable("subject") String subject) {
        return ResponseEntity.ok(contractApplicationService.getBySubject(subject));
    }

    @GetMapping("/getByProcedureType/{procedureType}")
    public ResponseEntity<List<GetContractDto>> getByProcedureType(
            @PathVariable("procedureType") String procedureType
    ) {
        return ResponseEntity.ok(contractApplicationService.getByProcedureType(procedureType));
    }

    @GetMapping("/getByPublicationDate/{publicationDate}")
    public ResponseEntity<List<GetContractDto>> getByPublicationDate(
            @PathVariable("publicationDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publicationDate
    ) {
        return ResponseEntity.ok(contractApplicationService.getByPublicationDate(publicationDate));
    }

    @GetMapping("/getByEstimatedValueVatGreaterThanEqual/{value}")
    public ResponseEntity<List<GetContractDto>> getByEstimatedValueVatGreaterThanEqual(
            @PathVariable("value") BigDecimal value
    ) {
        return ResponseEntity.ok(contractApplicationService.getByEstimatedValueVatGreaterThanEqual(value));
    }

    @GetMapping("/getByEstimatedValueVatLessThanEqual/{value}")
    public ResponseEntity<List<GetContractDto>> getByEstimatedValueVatLessThanEqual(
            @PathVariable("value") BigDecimal value
    ) {
        return ResponseEntity.ok(contractApplicationService.getByEstimatedValueVatLessThanEqual(value));
    }

    @GetMapping("/getByEstimatedValueVatBetween/{min}/{max}")
    public ResponseEntity<List<GetContractDto>> getByEstimatedValueVatBetween(
            @PathVariable("min") BigDecimal min,
            @PathVariable("max") BigDecimal max
    ) {
        return ResponseEntity.ok(contractApplicationService.getByEstimatedValueVatBetween(min, max));
    }

    @GetMapping("/getByContractValueVatGreaterThanEqual/{value}")
    public ResponseEntity<List<GetContractDto>> getByContractValueVatGreaterThanEqual(
            @PathVariable("value") BigDecimal value
    ) {
        return ResponseEntity.ok(contractApplicationService.getByContractValueVatGreaterThanEqual(value));
    }

    @GetMapping("/getByContractValueVatLessThanEqual/{value}")
    public ResponseEntity<List<GetContractDto>> getByContractValueVatLessThanEqual(
            @PathVariable("value") BigDecimal value
    ) {
        return ResponseEntity.ok(contractApplicationService.getByContractValueVatLessThanEqual(value));
    }

    @GetMapping("/getByContractValueVatBetween/{min}/{max}")
    public ResponseEntity<List<GetContractDto>> getByContractValueVatBetween(
            @PathVariable("min") BigDecimal min,
            @PathVariable("max") BigDecimal max
    ) {
        return ResponseEntity.ok(contractApplicationService.getByContractValueVatBetween(min, max));
    }

    @GetMapping("/allThatHaveDecision")
    public ResponseEntity<List<GetContractDto>> allThatHaveDecision() {
        return ResponseEntity.ok(contractApplicationService.allThatHaveDecision());
    }

    @GetMapping("/allThatDontHaveDecision")
    public ResponseEntity<List<GetContractDto>> allThatDontHaveDecision() {
        return ResponseEntity.ok(contractApplicationService.allThatDontHaveDecision());
    }

    @GetMapping("/allThatHaveRealizedContract")
    public ResponseEntity<List<GetContractDto>> allThatHaveRealizedContract() {
        return ResponseEntity.ok(contractApplicationService.allThatHaveRealizedContract());
    }

    @GetMapping("/allThatDontHaveRealizedContract")
    public ResponseEntity<List<GetContractDto>> allThatDontHaveRealizedContract() {
        return ResponseEntity.ok(contractApplicationService.allThatDontHaveRealizedContract());
    }

    @GetMapping("/allWhereContractValueExceedsEstimatedValue")
    public ResponseEntity<List<GetContractDto>> allWhereContractValueExceedsEstimatedValue() {
        return ResponseEntity.ok(contractApplicationService.allWhereContractValueExceedsEstimatedValue());
    }

    @GetMapping("/allWhereContractValueIsLessThanOrEqualToEstimatedValue")
    public ResponseEntity<List<GetContractDto>> allWhereContractValueIsLessThanOrEqualToEstimatedValue() {
        return ResponseEntity.ok(contractApplicationService.allWhereContractValueIsLessThanOrEqualToEstimatedValue());
    }


    @GetMapping("/search")
    public ResponseEntity<PagedResponseDto<GetContractTableRowDto>> search(
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) String noticeNumber,
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String contractType,
            @RequestParam(required = false) String procedureType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) BigDecimal minValue,
            @RequestParam(required = false) BigDecimal maxValue,
            @RequestParam(required = false) RiskLevel riskLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "contractDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(
                contractApplicationService.search(
                        searchText,
                        noticeNumber,
                        institutionId,
                        supplierId,
                        contractType,
                        procedureType,
                        dateFrom,
                        dateTo,
                        minValue,
                        maxValue,
                        riskLevel,
                        page,
                        size,
                        sortBy,
                        sortDir
                )
        );
    }

    @GetMapping("/paged")
    public ResponseEntity<PagedResponseDto<GetContractDto>> getAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(
                contractApplicationService.getAllPaginated(page, size, sortBy, sortDir)
        );
    }
}