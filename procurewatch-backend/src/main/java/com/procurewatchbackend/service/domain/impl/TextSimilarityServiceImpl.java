package com.procurewatchbackend.service.domain.impl;

import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.model.entity.Notice;
import com.procurewatchbackend.model.entity.PlanItem;
import com.procurewatchbackend.repository.ContractRepository;
import com.procurewatchbackend.repository.NoticeRepository;
import com.procurewatchbackend.repository.PlanItemRepository;
import com.procurewatchbackend.service.domain.TextSimilarityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TextSimilarityServiceImpl implements TextSimilarityService {

    private final ContractRepository contractRepository;
    private final NoticeRepository noticeRepository;
    private final PlanItemRepository planItemRepository;

    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\w+\\b");
    private static final int SIMILARITY_THRESHOLD = 30;
    private static final int TOP_RESULTS = 5;

    @Override
    public double compareTexts(String text1, String text2) {
        if (text1 == null || text2 == null || text1.isBlank() || text2.isBlank()) {
            return 0.0;
        }

        List<String> tokens1 = tokenize(text1);
        List<String> tokens2 = tokenize(text2);

        if (tokens1.isEmpty() || tokens2.isEmpty()) {
            return 0.0;
        }

        Map<String, Double> vector1 = calculateTfIdfVector(tokens1);
        Map<String, Double> vector2 = calculateTfIdfVector(tokens2);

        return cosineSimilarity(vector1, vector2);
    }

    @Override
    public List<Contract> findSimilarContracts(Long contractId) {
        Contract contract = contractRepository.findById(contractId).orElse(null);
        if (contract == null || contract.getSubject() == null) {
            return List.of();
        }

        List<Contract> allContracts = contractRepository.findAll();
        String cpvCode = contract.getSubject();

        return allContracts.stream()
                .filter(c -> !c.getId().equals(contractId))
                .filter(c -> c.getSubject() != null)
                .map(c -> new SimilarityPair<>(c, compareTexts(contract.getSubject(), c.getSubject())))
                .filter(p -> p.score >= SIMILARITY_THRESHOLD / 100.0)
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(TOP_RESULTS)
                .map(p -> p.entity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Notice> findSimilarNotices(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId).orElse(null);
        if (notice == null || notice.getSubject() == null) {
            return List.of();
        }

        List<Notice> allNotices = noticeRepository.findAll();

        return allNotices.stream()
                .filter(n -> !n.getId().equals(noticeId))
                .filter(n -> n.getSubject() != null)
                .map(n -> new SimilarityPair<>(n, compareTexts(notice.getSubject(), n.getSubject())))
                .filter(p -> p.score >= SIMILARITY_THRESHOLD / 100.0)
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(TOP_RESULTS)
                .map(p -> p.entity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlanItem> findSimilarPlanItems(Long planItemId) {
        PlanItem planItem = planItemRepository.findById(planItemId).orElse(null);
        if (planItem == null || planItem.getSubject() == null) {
            return List.of();
        }

        List<PlanItem> allPlanItems = planItemRepository.findAll();
        String cpvCode = planItem.getCpvCode();

        return allPlanItems.stream()
                .filter(p -> !p.getId().equals(planItemId))
                .filter(p -> p.getSubject() != null)
                .filter(p -> cpvCodesMatch(cpvCode, p.getCpvCode()))
                .map(p -> new SimilarityPair<>(p, compareTexts(planItem.getSubject(), p.getSubject())))
                .filter(sp -> sp.score >= SIMILARITY_THRESHOLD / 100.0)
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(TOP_RESULTS)
                .map(p -> p.entity)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<?>> findSimilarDocuments(Long contractId) {
        Contract contract = contractRepository.findById(contractId).orElse(null);
        if (contract == null) {
            return new HashMap<>();
        }

        Map<String, List<?>> result = new HashMap<>();
        result.put("contracts", findSimilarContracts(contractId));

        if (contract.getNoticeNumber() != null) {
            List<Notice> similarNotices = noticeRepository.findAll().stream()
                    .filter(n -> n.getSubject() != null && contract.getSubject() != null)
                    .filter(n -> compareTexts(contract.getSubject(), n.getSubject()) >= SIMILARITY_THRESHOLD / 100.0)
                    .limit(TOP_RESULTS)
                    .collect(Collectors.toList());
            result.put("notices", similarNotices);
        }

        List<PlanItem> similarPlanItems = planItemRepository.findAll().stream()
                .filter(p -> p.getSubject() != null && contract.getSubject() != null)
                .filter(p -> compareTexts(contract.getSubject(), p.getSubject()) >= SIMILARITY_THRESHOLD / 100.0)
                .limit(TOP_RESULTS)
                .collect(Collectors.toList());
        result.put("planItems", similarPlanItems);

        return result;
    }

    @Override
    public double calculateContractSimilarityScore(Long contractId) {
        Contract contract = contractRepository.findById(contractId).orElse(null);
        if (contract == null) {
            return 0.0;
        }

        List<Contract> similarContracts = findSimilarContracts(contractId);
        if (similarContracts.isEmpty()) {
            return 0.0;
        }

        double averageScore = similarContracts.stream()
                .mapToDouble(c -> compareTexts(contract.getSubject(), c.getSubject()))
                .average()
                .orElse(0.0);

        return Math.min(averageScore, 1.0);
    }

    private List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return WORD_PATTERN.matcher(text.toLowerCase())
                .results()
                .map(m -> m.group())
                .filter(word -> word.length() > 2)
                .collect(Collectors.toList());
    }

    private Map<String, Double> calculateTfIdfVector(List<String> tokens) {
        Map<String, Integer> termFrequency = new HashMap<>();
        for (String token : tokens) {
            termFrequency.put(token, termFrequency.getOrDefault(token, 0) + 1);
        }

        Map<String, Double> tfidfVector = new HashMap<>();
        int docLength = tokens.size();

        for (Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
            double tf = (double) entry.getValue() / docLength;
            double idf = Math.log(1.0 / (1.0 + entry.getValue()));
            tfidfVector.put(entry.getKey(), tf * idf);
        }

        return tfidfVector;
    }

    private double cosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
        Set<String> allTerms = new HashSet<>();
        allTerms.addAll(vector1.keySet());
        allTerms.addAll(vector2.keySet());

        if (allTerms.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;

        for (String term : allTerms) {
            double val1 = vector1.getOrDefault(term, 0.0);
            double val2 = vector2.getOrDefault(term, 0.0);

            dotProduct += val1 * val2;
            magnitude1 += val1 * val1;
            magnitude2 += val2 * val2;
        }

        magnitude1 = Math.sqrt(magnitude1);
        magnitude2 = Math.sqrt(magnitude2);

        if (magnitude1 == 0.0 || magnitude2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (magnitude1 * magnitude2);
    }

    private boolean cpvCodesMatch(String cpv1, String cpv2) {
        if (cpv1 == null || cpv1.isBlank() || cpv2 == null || cpv2.isBlank()) {
            return true;
        }
        return cpv1.length() >= 4 && cpv2.length() >= 4 &&
                cpv1.substring(0, 4).equals(cpv2.substring(0, 4));
    }

    private static class SimilarityPair<T> {
        T entity;
        double score;

        SimilarityPair(T entity, double score) {
            this.entity = entity;
            this.score = score;
        }
    }
}
