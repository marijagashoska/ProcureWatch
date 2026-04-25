package com.procurewatchbackend.ai;

import com.procurewatchbackend.model.entity.Contract;
import org.springframework.stereotype.Service;
import java.time.temporal.ChronoUnit;

@Service
public class FeatureService {
    public double[] getFeatures(Contract contract) {
        // 1. contractValueVat
        double value = contract.getContractValueVat().doubleValue();
        // 2. estimatedValueVat
        double estimated = contract.getEstimatedValueVat().doubleValue();
        // 3. contractToEstimateRatio
        double ratio = value / estimated;

        // Враќаме низа од броеви за AI моделот
        return new double[]{value, estimated, ratio};
    }
}