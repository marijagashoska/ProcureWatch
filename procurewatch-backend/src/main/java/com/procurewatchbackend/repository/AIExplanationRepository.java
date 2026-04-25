package com.procurewatchbackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.procurewatchbackend.model.entity.AIExplanation;

@Repository
public interface AIExplanationRepository extends JpaRepository<AIExplanation, Long> {

    Optional<AIExplanation> findTopByContractIdOrderByGeneratedAtDesc(Long contractId);

    List<AIExplanation> findByContractIdOrderByGeneratedAtDesc(Long contractId);
}