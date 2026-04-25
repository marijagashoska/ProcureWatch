package com.procurewatchbackend.ai;

import com.procurewatchbackend.model.entity.Contract;
import org.springframework.stereotype.Service;
import smile.anomaly.IsolationForest;
import lombok.RequiredArgsConstructor;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AnomalyDetectionService {
    private final FeatureService featureService;
    private IsolationForest model;

    public double calculateAnomaly(Contract contract) {
        double[] features = featureService.getFeatures(contract);

        // Ако немаме доволно податоци за тренирање, користиме пресметка
        if (model == null) {
            double ratio = features[2]; // contractToEstimateRatio
            return (ratio > 1.4) ? 0.85 : 0.20;
        }

        return model.score(features); // anomalyScore
    }
}