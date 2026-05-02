package com.procurewatchbackend.ai;

import com.procurewatchbackend.model.entity.Contract;
import org.springframework.stereotype.Service;
import java.time.temporal.ChronoUnit;

@Service
public class FeatureService {

    public double[] getFeatures(Contract contract) {
        // 1. Износ на договорот (contractValueVat)
        double value = contract.getContractValueVat() != null ?
                contract.getContractValueVat().doubleValue() : 0.0;

        // 2. Проценета вредност (estimatedValueVat)
        double estimated = contract.getEstimatedValueVat() != null ?
                contract.getEstimatedValueVat().doubleValue() : 1.0;

        // 3. Однос на вредностите (contractToEstimateRatio)
        // Клучен индикатор за аномалија ако е премногу висок или низок
        double ratio = value / estimated;

        // 4. Временска анализа (daysNoticeToContract)
        // Пресметуваме колку денови поминале од огласот до потпишувањето
        double daysNoticeToContract = 30.0; // Просек ако фалат податоци
        if (contract.getNotice() != null && contract.getNotice().getPublicationDate() != null
                && contract.getContractDate() != null) {

            daysNoticeToContract = ChronoUnit.DAYS.between(
                    contract.getNotice().getPublicationDate(),
                    contract.getContractDate()
            );
        }

        // Враќаме 4 карактеристики (features) наместо 3.
        // Повеќе димензии значат попрецизен Isolation Forest.
        return new double[]{value, estimated, ratio, daysNoticeToContract};
    }
}