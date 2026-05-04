package com.procurewatchbackend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportLifecycleItemDto {

    private String phase;
    private String status;
    private LocalDate date;
    private String description;
}