package com.procurewatchbackend.service.application;

import com.procurewatchbackend.dto.display.GetContractLifecycleDto;

public interface LifecycleApplicationService {

    GetContractLifecycleDto getByContractId(Long contractId);
}