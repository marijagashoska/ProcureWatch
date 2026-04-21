package com.procurewatchbackend.dto.create;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CreateNoticeDto(
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
