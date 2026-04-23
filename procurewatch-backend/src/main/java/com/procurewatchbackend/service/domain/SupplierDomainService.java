package com.procurewatchbackend.service.domain;

import com.procurewatchbackend.dto.create.CreateSupplierDto;
import com.procurewatchbackend.dto.edit.EditSupplierDto;
import com.procurewatchbackend.model.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SupplierDomainService {

    Supplier add(CreateSupplierDto dto);

    Supplier edit(Long id, EditSupplierDto dto);

    void delete(Long id);

    List<Supplier> getAll();

    Supplier getById(Long id);

    List<Supplier> getByOfficialName(String officialName);

    List<Supplier> getByNormalizedName(String normalizedName);

    Supplier getByExternalId(String externalId);

    List<Supplier> getByRealOwnersInfo(String realOwnersInfo);

    List<Supplier> allThatHaveDecision();

    List<Supplier> allThatDontHaveDecision();

    Page<Supplier> getAllPaginated(Pageable pageable);

}