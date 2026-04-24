package com.procurewatchbackend.service.domain;

import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.model.entity.Notice;
import com.procurewatchbackend.model.entity.PlanItem;

import java.util.List;
import java.util.Map;

public interface TextSimilarityService {

    double compareTexts(String text1, String text2);

    List<Contract> findSimilarContracts(Long contractId);

    List<Notice> findSimilarNotices(Long noticeId);

    List<PlanItem> findSimilarPlanItems(Long planItemId);

    Map<String, List<?>> findSimilarDocuments(Long contractId);

    double calculateContractSimilarityScore(Long contractId);
}