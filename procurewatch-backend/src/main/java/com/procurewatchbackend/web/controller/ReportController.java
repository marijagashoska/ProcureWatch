package com.procurewatchbackend.web.controller;

import com.procurewatchbackend.dto.report.ContractReportDto;
import com.procurewatchbackend.dto.report.HighRiskContractReportDto;
import com.procurewatchbackend.service.application.ReportApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportApplicationService reportApplicationService;

    @GetMapping("/contracts/{contractId}")
    public ContractReportDto getContractReport(@PathVariable Long contractId) {
        return reportApplicationService.getContractReport(contractId);
    }

    @GetMapping("/high-risk")
    public List<HighRiskContractReportDto> getHighRiskContractsReport() {
        return reportApplicationService.getHighRiskContractsReport();
    }

    @GetMapping(value = "/contracts/{contractId}/export/csv", produces = "text/csv")
    public ResponseEntity<String> exportContractReportCsv(@PathVariable Long contractId) {
        String csv = reportApplicationService.exportContractReportCsv(contractId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("contract-report-" + contractId + ".csv")
                                .build()
                                .toString())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping(value = "/high-risk/export/csv", produces = "text/csv")
    public ResponseEntity<String> exportHighRiskContractsCsv() {
        String csv = reportApplicationService.exportHighRiskContractsCsv();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("high-risk-contracts-report.csv")
                                .build()
                                .toString())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}