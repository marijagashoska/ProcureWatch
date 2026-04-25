package com.procurewatchbackend.service.domain;

import java.util.List;

import com.procurewatchbackend.model.entity.AIExplanation;

public interface AIExplanationDomainService {

    AIExplanation generateForContract(Long contractId);

    AIExplanation getLatestByContractId(Long contractId);

    List<AIExplanation> getHistoryByContractId(Long contractId);
}