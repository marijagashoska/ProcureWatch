package com.procurewatchbackend.service.application;

import com.procurewatchbackend.dto.report.ContractReportDto;
import com.procurewatchbackend.dto.report.HighRiskContractReportDto;

import java.util.List;

public interface ReportApplicationService {

    ContractReportDto getContractReport(Long contractId);

    List<HighRiskContractReportDto> getHighRiskContractsReport();

    String exportContractReportCsv(Long contractId);

    String exportHighRiskContractsCsv();
}