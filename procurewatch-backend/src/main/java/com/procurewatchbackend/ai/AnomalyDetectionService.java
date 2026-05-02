package com.procurewatchbackend.ai;

import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.repository.ContractRepository;
import org.springframework.stereotype.Service;
import smile.anomaly.IsolationForest;
import lombok.RequiredArgsConstructor;
import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnomalyDetectionService {

    private final FeatureService featureService;
    private final ContractRepository contractRepository; // Ни треба за да ги повлечеме историските податоци
    private IsolationForest model;

    /**
     * МОДУЛ 7: Автоматско тренирање на моделот при стартување.
     * Ова го прави моделот "вистински" бидејќи учи од постоечките договори во базата.
     */
    @PostConstruct
    public void trainModelOnStartup() {
        try {
            List<Contract> allContracts = contractRepository.findAll();

            if (allContracts.size() < 5) {
                System.out.println("AI INFO: Нема доволно податоци во базата за вистински тренинг. Се користи демо логика.");
                return;
            }

            //Data Pipeline
            double[][] trainingData = new double[allContracts.size()][3];
            for (int i = 0; i < allContracts.size(); i++) {
                trainingData[i] = featureService.getFeatures(allContracts.get(i));
            }

            //Креирање на Properties објект наместо обичен број
            java.util.Properties props = new java.util.Properties();
            props.setProperty("smile.anomaly.isolation.forest.trees", "100"); // Број на стебла
            props.setProperty("smile.anomaly.isolation.forest.sample", "256"); // Големина на примерок

            this.model = IsolationForest.fit(trainingData, props);

            System.out.println("AI SUCCESS: Isolation Forest е успешно трениран на " + allContracts.size() + " записи.");

        } catch (Exception e) {
            System.err.println("AI ERROR: Грешка при тренирање на моделот: " + e.getMessage());
        }
    }

    /**
     * Пресметка на аномалија. Ако моделот е трениран, користи ML.
     * Ако не, користи базична логика на прагови.
     */
    public double calculateAnomaly(Contract contract) {
        double[] features = featureService.getFeatures(contract);

        // Ако моделот е успешно трениран, користи го неговиот score (0.0 до 1.0)
        if (model != null) {
            return model.score(features);
        }

        // Демо логика (Fallback) додека базата е празна
        double ratio = features[2]; // contractToEstimateRatio
        return (ratio > 1.4) ? 0.85 : 0.20;
    }
}