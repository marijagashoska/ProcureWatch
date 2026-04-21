package com.procurewatchbackend.dto.edit;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EditNoticeDto(
        Long institutionId,
        Long planItemId,
        String noticeNumber,
        String subject,
        String contractType,
        String procedureType,
        LocalDate publicationDate,
        LocalDateTime deadlineDate,
        String sourceUrl
) {
}