package com.procurewatchbackend.service.application;

import com.procurewatchbackend.dto.display.detail.GetContractDetailDto;

public interface ContractDetailApplicationService {

    GetContractDetailDto getContractDetail(
            Long contractId,
            boolean evaluateRiskIfMissing,
            boolean includeSimilar
    );
}