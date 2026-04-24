package com.procurewatchbackend.dto.importer;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class ImportResultDto {

    private int institutionsImported;
    private int suppliersImported;
    private int procurementPlansImported;
    private int planItemsImported;
    private int noticesImported;
    private int decisionsImported;
    private int contractsImported;
    private int realizedContractsImported;
    private int riskAssessmentsGenerated;
    private int skippedRows;

    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    public void warn(String message) {
        warnings.add(message);
    }
}