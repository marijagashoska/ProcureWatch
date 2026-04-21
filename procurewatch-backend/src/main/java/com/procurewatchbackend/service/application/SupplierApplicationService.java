package com.procurewatchbackend.service.application;

import com.procurewatchbackend.dto.create.CreateSupplierDto;
import com.procurewatchbackend.dto.display.GetSupplierDto;
import com.procurewatchbackend.dto.edit.EditSupplierDto;

import java.util.List;

public interface SupplierApplicationService {

    GetSupplierDto add(CreateSupplierDto dto);

    GetSupplierDto edit(Long id, EditSupplierDto dto);

    void delete(Long id);

    List<GetSupplierDto> getAll();

    GetSupplierDto getById(Long id);

    List<GetSupplierDto> getByOfficialName(String officialName);

    List<GetSupplierDto> getByNormalizedName(String normalizedName);

    GetSupplierDto getByExternalId(String externalId);

    List<GetSupplierDto> getByRealOwnersInfo(String realOwnersInfo);

    List<GetSupplierDto> allThatHaveDecision();

    List<GetSupplierDto> allThatDontHaveDecision();
}