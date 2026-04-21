package com.procurewatchbackend.repository;

import com.procurewatchbackend.model.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    Optional<Institution> findByExternalId(String externalId);

    List<Institution> findByCityContainingIgnoreCase(String city);

    List<Institution> findByInstitutionTypeIgnoreCase(String institutionType);

    List<Institution> findByCategoryIgnoreCase(String category);

    List<Institution> findByOfficialNameContainingIgnoreCase(String officialName);

    List<Institution> findByNormalizedNameContainingIgnoreCase(String normalizedName);
}