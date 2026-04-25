package com.procurewatchbackend.ai;

import org.springframework.stereotype.Service;

@Service
public class ExternalAiService {

    public String generateSummary(double score) {
        return score > 70 ? "Висок степен на аномалија во вредноста." : "Нормални вредности на договорот.";
    }

    public String generateExplanation(double score) {
        return score > 70 ? "Цената отстапува значително од проценетата вредност според Isolation Forest." : "Нема девијации.";
    }

    public String generateRecommendation(double score) {
        return score > 70 ? "Потребна е итна ревизија од човечки аналитичар." : "Не е потребна акција.";
    }
}