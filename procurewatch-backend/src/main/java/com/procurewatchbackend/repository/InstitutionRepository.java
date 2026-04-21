package com.procurewatchbackend.repository;

import com.procurewatchbackend.model.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstitutionRepository  extends JpaRepository<Institution, Long> {
}
