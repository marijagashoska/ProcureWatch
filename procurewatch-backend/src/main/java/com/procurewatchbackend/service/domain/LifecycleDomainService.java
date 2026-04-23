package com.procurewatchbackend.service.domain;

import com.procurewatchbackend.service.domain.model.ContractLifecycleSnapshot;

public interface LifecycleDomainService {

    ContractLifecycleSnapshot getByContractId(Long contractId);
}