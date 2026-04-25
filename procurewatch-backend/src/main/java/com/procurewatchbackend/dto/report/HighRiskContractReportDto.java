package com.procurewatchbackend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HighRiskContractReportDto {

    private Long contractId;

    private String subject;
    private String institutionName;
    private String supplierName;

    private LocalDate contractDate;
    private BigDecimal contractValueVat;
    private String currency;

    private BigDecimal finalRiskScore;
    private String riskLevel;
    private Integer priorityRank;

    private List<String> triggeredRiskFlags;

    private String explanationText;
}