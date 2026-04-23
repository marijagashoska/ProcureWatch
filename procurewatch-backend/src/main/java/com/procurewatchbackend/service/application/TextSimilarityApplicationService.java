package com.procurewatchbackend.service.application;

import com.procurewatchbackend.dto.display.GetContractDto;
import com.procurewatchbackend.dto.display.GetNoticeDto;
import com.procurewatchbackend.dto.display.GetPlanItemDto;

import java.util.List;
import java.util.Map;

public interface TextSimilarityApplicationService {

    double compareTexts(String text1, String text2);

    List<GetContractDto> findSimilarContracts(Long contractId);

    List<GetNoticeDto> findSimilarNotices(Long noticeId);

    List<GetPlanItemDto> findSimilarPlanItems(Long planItemId);

    Map<String, Object> findSimilarDocuments(Long contractId);

    SimilarityScoreDto calculateContractSimilarityScore(Long contractId);

    record SimilarityScoreDto(
            double score,
            String percentage
    ) {
    }
}

