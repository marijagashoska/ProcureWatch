package com.procurewatchbackend.repository;

import com.procurewatchbackend.model.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByExternalId(String externalId);

    List<Supplier> findByOfficialNameContainingIgnoreCase(String officialName);

    List<Supplier> findByNormalizedNameContainingIgnoreCase(String normalizedName);

    List<Supplier> findByRealOwnersInfoContainingIgnoreCase(String realOwnersInfo);

    @Query("select distinct s from Supplier s where s.decisions is not empty")
    List<Supplier> findAllThatHaveDecision();

    @Query("select distinct s from Supplier s where s.decisions is empty")
    List<Supplier> findAllThatDontHaveDecision();
}