package com.procurewatchbackend.web.controller;

import com.procurewatchbackend.dto.create.CreateNoticeDto;
import com.procurewatchbackend.dto.display.GetContractDto;
import com.procurewatchbackend.dto.display.GetNoticeDto;
import com.procurewatchbackend.dto.display.PagedResponseDto;
import com.procurewatchbackend.dto.edit.EditNoticeDto;
import com.procurewatchbackend.service.application.NoticeApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeApplicationService noticeApplicationService;

    @PostMapping("/add")
    public ResponseEntity<GetNoticeDto> add(@RequestBody CreateNoticeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(noticeApplicationService.add(dto));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<GetNoticeDto> edit(
            @PathVariable("id") Long id,
            @RequestBody EditNoticeDto dto
    ) {
        return ResponseEntity.ok(noticeApplicationService.edit(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        noticeApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/allByInstitution/{institutionId}")
    public ResponseEntity<List<GetNoticeDto>> allByInstitution(@PathVariable("institutionId") Long institutionId) {
        return ResponseEntity.ok(noticeApplicationService.allByInstitution(institutionId));
    }

    @GetMapping("/allBySubject/{subject}")
    public ResponseEntity<List<GetNoticeDto>> allBySubject(@PathVariable("subject") String subject) {
        return ResponseEntity.ok(noticeApplicationService.allBySubject(subject));
    }

    @GetMapping("/allByContractType/{contractType}")
    public ResponseEntity<List<GetNoticeDto>> allByContractType(@PathVariable("contractType") String contractType) {
        return ResponseEntity.ok(noticeApplicationService.allByContractType(contractType));
    }

    @GetMapping("/allByProcedureType/{procedureType}")
    public ResponseEntity<List<GetNoticeDto>> allByProcedureType(@PathVariable("procedureType") String procedureType) {
        return ResponseEntity.ok(noticeApplicationService.allByProcedureType(procedureType));
    }


    //OVIE FUNKCII MOZHAT SAMO DA PRIMAAT BOOL PARAMETAR true/false NO NEKA SEDAT ZASEGA
    @GetMapping("/allThatHavePlanItem")
    public ResponseEntity<List<GetNoticeDto>> allThatHavePlanItem() {
        return ResponseEntity.ok(noticeApplicationService.allThatHavePlanItem());
    }

    @GetMapping("/allThatDontHavePlanItem")
    public ResponseEntity<List<GetNoticeDto>> allThatDontHavePlanItem() {
        return ResponseEntity.ok(noticeApplicationService.allThatDontHavePlanItem());
    }

    @GetMapping("/allThatHaveDession")
    public ResponseEntity<List<GetNoticeDto>> allThatHaveDecision() {
        return ResponseEntity.ok(noticeApplicationService.allThatHaveDecision());
    }

    @GetMapping("/AllThatDontHaveDecision")
    public ResponseEntity<List<GetNoticeDto>> allThatDontHaveDecision() {
        return ResponseEntity.ok(noticeApplicationService.allThatDontHaveDecision());
    }

    @GetMapping("/paged")
    public ResponseEntity<PagedResponseDto<GetNoticeDto>> getAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(noticeApplicationService.getAllPaginated(page, size, sortBy, sortDir)
        );
    }
}