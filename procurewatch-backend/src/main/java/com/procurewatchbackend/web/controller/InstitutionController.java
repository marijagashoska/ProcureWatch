package com.procurewatchbackend.web.controller;

import com.procurewatchbackend.dto.create.CreateInstitutionDto;
import com.procurewatchbackend.dto.display.GetInstitutionDto;
import com.procurewatchbackend.dto.edit.EditInstitutionDto;
import com.procurewatchbackend.service.application.InstitutionApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/institutions")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionApplicationService institutionApplicationService;

    @PostMapping("/add")
    public ResponseEntity<GetInstitutionDto> add(@RequestBody CreateInstitutionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(institutionApplicationService.add(dto));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<GetInstitutionDto> edit(
            @PathVariable("id") Long id,
            @RequestBody EditInstitutionDto dto
    ) {
        return ResponseEntity.ok(institutionApplicationService.edit(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        institutionApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<GetInstitutionDto>> getAll() {
        return ResponseEntity.ok(institutionApplicationService.getAll());
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<GetInstitutionDto> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(institutionApplicationService.getById(id));
    }

    @GetMapping("/getByCity/{city}")
    public ResponseEntity<List<GetInstitutionDto>> getByCity(@PathVariable("city") String city) {
        return ResponseEntity.ok(institutionApplicationService.getByCity(city));
    }

    @GetMapping("/getByInstType/{institutionType}")
    public ResponseEntity<List<GetInstitutionDto>> getByInstType(
            @PathVariable("institutionType") String institutionType
    ) {
        return ResponseEntity.ok(institutionApplicationService.getByInstType(institutionType));
    }

    @GetMapping("/getByCategory/{category}")
    public ResponseEntity<List<GetInstitutionDto>> getByCategory(@PathVariable("category") String category) {
        return ResponseEntity.ok(institutionApplicationService.getByCategory(category));
    }

    @GetMapping("/getByOfficialName/{officialName}")
    public ResponseEntity<List<GetInstitutionDto>> getByOfficialName(
            @PathVariable("officialName") String officialName
    ) {
        return ResponseEntity.ok(institutionApplicationService.getByOfficialName(officialName));
    }

    @GetMapping("/getByNormalizedName/{normalizedName}")
    public ResponseEntity<List<GetInstitutionDto>> getByNormalizedName(
            @PathVariable("normalizedName") String normalizedName
    ) {
        return ResponseEntity.ok(institutionApplicationService.getByNormalizedName(normalizedName));
    }
}