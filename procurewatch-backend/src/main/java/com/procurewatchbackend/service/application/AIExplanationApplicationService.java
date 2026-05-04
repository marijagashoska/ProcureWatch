package com.procurewatchbackend.service.application;

import java.util.List;

import com.procurewatchbackend.dto.display.GetAIExplanationDto;

public interface AIExplanationApplicationService {

    GetAIExplanationDto generateForContract(Long contractId);

    GetAIExplanationDto getLatestByContractId(Long contractId);

    List<GetAIExplanationDto> getHistoryByContractId(Long contractId);
}