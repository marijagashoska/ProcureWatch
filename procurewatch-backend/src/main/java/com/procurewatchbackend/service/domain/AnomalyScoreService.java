package com.procurewatchbackend.service.domain;

public interface AnomalyScoreService {
    double calculateAnomalyScore(Long contractId);
}