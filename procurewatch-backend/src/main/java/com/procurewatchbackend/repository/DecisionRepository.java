package com.procurewatchbackend.repository;

import com.procurewatchbackend.model.entity.Decision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DecisionRepository extends JpaRepository<Decision, Long> {
}
