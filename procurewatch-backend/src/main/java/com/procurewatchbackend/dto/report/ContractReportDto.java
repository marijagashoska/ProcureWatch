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
public class ContractReportDto {

    private Long contractId;

    private String noticeNumber;
    private String subject;
    private String contractType;
    private String procedureType;

    private LocalDate contractDate;
    private LocalDate publicationDate;

    private BigDecimal estimatedValueVat;
    private BigDecimal contractValueVat;
    private String currency;

    private String institutionName;
    private String supplierName;

    private BigDecimal ruleScore;
    private BigDecimal anomalyScore;
    private BigDecimal similarityScore;
    private BigDecimal clusterScore;
    private BigDecimal finalRiskScore;
    private String riskLevel;
    private Integer priorityRank;

    private List<String> triggeredRiskFlags;

    private String summaryText;
    private String explanationText;
    private String recommendationText;

    private List<ReportLifecycleItemDto> lifecycleData;
}