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

        return browserClient.scrapeList(route, maxPages)
                .stream()
                .limit(limit)
                .map(this::debugRow)
                .toList();
    }

    @GetMapping("/debug/url")
    public List<Map<String, Object>> debugScrapeDirectUrl(
            @RequestParam String url,
            @RequestParam(defaultValue = "1") int maxPages,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return browserClient.scrapeList(url, maxPages)
                .stream()
                .limit(limit)
                .map(this::debugRow)
                .toList();
    }

    @GetMapping("/debug/plan-items")
    public Map<String, Object> debugScrapePlanItems(
            @RequestParam(defaultValue = "1") int maxPages,
            @RequestParam(defaultValue = "25") int maxAnnualPlanDetails,
            @RequestParam(defaultValue = "200") int limit
    ) {
        List<ScrapedRow> annualPlanRows = browserClient.scrapeList("/annual-plans", maxPages);

        List<ScrapedRow> planItemRows =
                browserClient.scrapeAnnualPlanItemDetails(annualPlanRows, maxAnnualPlanDetails);

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("annualPlansScraped", annualPlanRows.size());
        output.put("planItemsScraped", planItemRows.size());
        output.put("maxPages", maxPages);
        output.put("maxAnnualPlanDetails", maxAnnualPlanDetails);
        output.put("limit", limit);

        output.put("annualPlans", annualPlanRows.stream()
                .limit(Math.min(limit, 20))
                .map(this::debugRow)
                .toList());

        output.put("planItems", planItemRows.stream()
                .limit(limit)
                .map(this::debugRow)
                .toList());

        return output;
    }

    private Map<String, Object> debugRow(ScrapedRow row) {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("sourceUrl", row.sourceUrl());
        output.put("fields", row.fields());
        return output;
    }
}