package com.procurewatchbackend.web.controller;

import com.procurewatchbackend.dto.importer.ImportResultDto;
import com.procurewatchbackend.scraper.ENabavkiBrowserClient;
import com.procurewatchbackend.scraper.ScrapedRow;
import com.procurewatchbackend.service.importer.ENabavkiFullImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/import/e-nabavki")
@RequiredArgsConstructor
public class ImportController {

    private final ENabavkiFullImportService importService;
    private final ENabavkiBrowserClient browserClient;

    @PostMapping("/full")
    public ImportResultDto importFull(
            @RequestParam(required = false) Integer fromYear,
            @RequestParam(required = false) Integer toYear,
            @RequestParam(defaultValue = "3") int maxPages,
            @RequestParam(defaultValue = "25") int maxAnnualPlanDetails,
            @RequestParam(defaultValue = "true") boolean evaluateRisk
    ) {
        return importService.importAll(
                fromYear,
                toYear,
                maxPages,
                maxAnnualPlanDetails,
                evaluateRisk
        );
    }

    @GetMapping("/debug")
    public List<Map<String, Object>> debugScrape(
            @RequestParam(defaultValue = "contracts") String source,
            @RequestParam(defaultValue = "1") int maxPages,
            @RequestParam(defaultValue = "10") int limit
    ) {
        String route = switch (source) {
            case "annual-plans" -> "/annual-plans";
            case "notices" -> "/notices";
            case "decisions" -> "/tender-winners/0";
            case "contracts" -> "/contracts/0";
            case "realized-contracts" -> "/realized-contract";
            default -> throw new IllegalArgumentException("Unknown source: " + source);
        };

        List<ScrapedRow> rows = browserClient.scrapeList(route, maxPages);

        return rows.stream()
                .limit(limit)
                .map(row -> {
                    Map<String, Object> output = new LinkedHashMap<>();
                    output.put("sourceUrl", row.sourceUrl());
                    output.put("fields", row.fields());
                    return output;
                })
                .toList();
    }
}