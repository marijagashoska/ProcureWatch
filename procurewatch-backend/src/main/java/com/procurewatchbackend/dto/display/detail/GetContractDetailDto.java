package com.procurewatchbackend.dto.display.detail;

import com.procurewatchbackend.dto.display.GetContractDto;
import com.procurewatchbackend.dto.display.GetContractLifecycleDto;
import com.procurewatchbackend.dto.display.GetInstitutionDto;
import com.procurewatchbackend.dto.display.GetRealizedContractDto;
import com.procurewatchbackend.dto.display.GetRiskAssessmentDto;
import com.procurewatchbackend.dto.display.GetSupplierDto;

import java.util.Map;

public record GetContractDetailDto(
        Long contractId,
        String title,
        GetContractDto contract,
        GetInstitutionDto institution,
        GetSupplierDto supplier,
        GetContractLifecycleDto lifecycle,
        GetRiskAssessmentDto riskAssessment,
        ContractDetailExplanationDto explanation,
        GetRealizedContractDto realizedContract,
        Map<String, Object> similarDocuments
) {
}