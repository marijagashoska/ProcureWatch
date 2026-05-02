package com.procurewatchbackend.service.domain.impl;

import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.repository.ContractRepository;
import com.procurewatchbackend.service.domain.AnomalyScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnomalyScoreServiceImpl implements AnomalyScoreService {

    private final ContractRepository contractRepository;

    @Override
    public double calculateAnomalyScore(Long contractId) {
        Contract contract = contractRepository.findById(contractId).orElse(null);
        if (contract == null || contract.getContractValueVat() == null) {
            return 0.0;
        }

        if (contract.getInstitution() == null) {
            return 0.0;
        }

        Long institutionId = contract.getInstitution().getId();
        List<Contract> peerContracts = contractRepository.findByInstitutionId(institutionId)
                .stream()
                .filter(c -> !c.getId().equals(contractId))
                .filter(c -> c.getContractValueVat() != null)
                .toList();

        if (peerContracts.size() < 3) {
            return 0.0;
        }

        double[] values = peerContracts.stream()
                .mapToDouble(c -> c.getContractValueVat().doubleValue())
                .toArray();

        double mean = calculateMean(values);
        double stdDev = calculateStdDev(values, mean);

        if (stdDev == 0.0) {
            return 0.0;
        }

        double contractValue = contract.getContractValueVat().doubleValue();
        double zScore = Math.abs((contractValue - mean) / stdDev);

// Колку подалеку е од просекот, толку поголем ризик (макс 100)
        double normalized = Math.min((zScore / 3.0) * 100.0, 100.0);

        return Math.round(normalized * 100.0) / 100.0;
    }

    private double calculateMean(double[] values) {
        double sum = 0.0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.length;
    }

    private double calculateStdDev(double[] values, double mean) {
        double sumSquares = 0.0;
        for (double v : values) {
            sumSquares += Math.pow(v - mean, 2);
        }
        return Math.sqrt(sumSquares / values.length);
    }
}