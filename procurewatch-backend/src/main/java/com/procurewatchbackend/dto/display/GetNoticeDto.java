package com.procurewatchbackend.dto.display;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record GetNoticeDto(
        Long id,
        Long institutionId,
        Long planItemId,
        List<Long> decisionIds,
        String noticeNumber,
        String subject,
        String contractType,
        String procedureType,
        LocalDate publicationDate,
        LocalDateTime deadlineDate,
        String sourceUrl
) {
}