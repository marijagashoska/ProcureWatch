package com.procurewatchbackend.scraper;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import com.procurewatchbackend.util.MojibakeFixer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ENabavkiBrowserClient {

    @Value("${procurewatch.scraper.base-url:https://www.e-nabavki.gov.mk/PublicAccess/home.aspx#}")
    private String baseUrl;

    @Value("${procurewatch.scraper.headless:true}")
    private boolean headless;

    @Value("${procurewatch.scraper.navigation-timeout-ms:60000}")
    private int navigationTimeoutMs;

    public List<ScrapedRow> scrapeList(String route, int maxPages) {
        String url = route.startsWith("http") ? route : baseUrl + route;

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions().setHeadless(headless)
             )) {

            Page page = browser.newPage();
            page.setDefaultTimeout(navigationTimeoutMs);

            page.navigate(url);
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // Give the Angular page a little more time after NETWORKIDLE.
            page.waitForTimeout(2_000);

            tryClickSearchButton(page);

            return scrapePagedTables(page, maxPages);
        }
    }

    public List<ScrapedRow> scrapeAnnualPlanItemDetails(List<ScrapedRow> annualPlans, int maxDetails) {
        List<ScrapedRow> allItems = new ArrayList<>();
        int opened = 0;

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions().setHeadless(headless)
             )) {

            Page page = browser.newPage();
            page.setDefaultTimeout(navigationTimeoutMs);

            for (ScrapedRow annualPlan : annualPlans) {
                if (annualPlan.sourceUrl() == null || annualPlan.sourceUrl().isBlank()) {
                    continue;
                }

                if (maxDetails > 0 && opened >= maxDetails) {
                    break;
                }

                page.navigate(absoluteUrl(annualPlan.sourceUrl()));
                page.waitForLoadState(LoadState.NETWORKIDLE);
                page.waitForTimeout(2_000);

                tryClickSearchButton(page);

                List<ScrapedRow> detailRows = readRowsFromTables(page);

                String parentInstitution = annualPlan.get(
                        "Назив на договорниот орган",
                        "Договорен орган",
                        "Институција",
                        "Договорен орган/Институција",
                        "Contracting authority",
                        "Institution"
                );

                String parentYear = annualPlan.get(
                        "Година",
                        "План за година",
                        "Година на план",
                        "year"
                );

                String parentPublicationDate = annualPlan.get(
                        "Датум на објава",
                        "Датум на објавување",
                        "Датум",
                        "publicationDate"
                );

                for (ScrapedRow row : detailRows) {
                    allItems.add(row
                            .with("_parentInstitution", parentInstitution)
                            .with("_parentYear", parentYear)
                            .with("_parentPublicationDate", parentPublicationDate)
                            .with("_parentSourceUrl", annualPlan.sourceUrl()));
                }

                opened++;
            }
        }

        return allItems;
    }

    private List<ScrapedRow> scrapePagedTables(Page page, int maxPages) {
        List<ScrapedRow> rows = new ArrayList<>();
        int pageNumber = 0;

        while (true) {
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(1_000);

            rows.addAll(readRowsFromTables(page));

            pageNumber++;

            if (maxPages > 0 && pageNumber >= maxPages) {
                break;
            }

            if (!clickNextPage(page)) {
                break;
            }
        }

        return rows;
    }

    private List<ScrapedRow> readRowsFromTables(Page page) {
        List<ScrapedRow> rows = new ArrayList<>();
        Locator tables = page.locator("table");

        for (int tableIndex = 0; tableIndex < tables.count(); tableIndex++) {
            Locator table = tables.nth(tableIndex);

            String tableText;
            try {
                tableText = table.innerText();
            } catch (Exception e) {
                continue;
            }

            List<String> headers = readHeaders(table);

            if (isCalendarTable(headers, tableText)) {
                continue;
            }

            if (!looksLikeProcurementTable(headers, tableText)) {
                continue;
            }

            Locator tableRows = table.locator("tbody tr");
            int startIndex = 0;

            if (tableRows.count() == 0) {
                tableRows = table.locator("tr");
                startIndex = headers.isEmpty() ? 0 : 1;
            }

            for (int rowIndex = startIndex; rowIndex < tableRows.count(); rowIndex++) {
                Locator row = tableRows.nth(rowIndex);
                Locator cells = row.locator("td");

                if (cells.count() == 0) {
                    continue;
                }

                Map<String, String> fields = new LinkedHashMap<>();

                for (int cellIndex = 0; cellIndex < cells.count(); cellIndex++) {
                    String header = cellIndex < headers.size() && !headers.get(cellIndex).isBlank()
                            ? headers.get(cellIndex)
                            : "column_" + cellIndex;

                    String value;
                    try {
                        value = cells.nth(cellIndex).innerText().trim();
                    } catch (Exception e) {
                        value = "";
                    }

                    fields.put(
                            MojibakeFixer.fix(header),
                            MojibakeFixer.fix(value)
                    );
                }

                if (isCalendarRow(fields)) {
                    continue;
                }

                if (isEmptyRow(fields)) {
                    continue;
                }

                String rowSourceUrl = firstHref(row);

                rows.add(new ScrapedRow(
                        fields,
                        rowSourceUrl == null ? page.url() : absoluteUrl(rowSourceUrl)
                ));
            }
        }

        return rows;
    }

    private List<String> readHeaders(Locator table) {
        List<String> headers = new ArrayList<>();

        Locator th = table.locator("thead th");

        if (th.count() == 0) {
            try {
                th = table.locator("tr").first().locator("th");
            } catch (Exception ignored) {
                return headers;
            }
        }

        if (th.count() == 0) {
            try {
                th = table.locator("tr").first().locator("td");
            } catch (Exception ignored) {
                return headers;
            }
        }

        for (int i = 0; i < th.count(); i++) {
            try {
                headers.add(MojibakeFixer.fix(th.nth(i).innerText().trim()));
            } catch (Exception ignored) {
                headers.add("");
            }
        }

        return headers;
    }

    private boolean clickNextPage(Page page) {
        String selector = String.join(",",
                "a:has-text('Следна')",
                "button:has-text('Следна')",
                "a:has-text('Напред')",
                "button:has-text('Напред')",
                "a:has-text('Next')",
                "button:has-text('Next')",
                "a[aria-label='Next']",
                "button[aria-label='Next']",
                ".pagination-next a",
                ".pagination-next button"
        );

        Locator candidates = page.locator(selector);

        for (int i = 0; i < candidates.count(); i++) {
            Locator candidate = candidates.nth(i);

            try {
                String disabled = candidate.getAttribute("disabled");
                String ariaDisabled = candidate.getAttribute("aria-disabled");
                String className = candidate.getAttribute("class");

                if (disabled != null) {
                    continue;
                }

                if ("true".equalsIgnoreCase(ariaDisabled)) {
                    continue;
                }

                if (className != null && className.toLowerCase().contains("disabled")) {
                    continue;
                }

                if (!candidate.isVisible()) {
                    continue;
                }

                candidate.click();
                page.waitForLoadState(LoadState.NETWORKIDLE);
                page.waitForTimeout(1_000);

                return true;
            } catch (Exception ignored) {
            }
        }

        return false;
    }

    private void tryClickSearchButton(Page page) {
        String selector = String.join(",",
                "button:has-text('Пребарај')",
                "a:has-text('Пребарај')",
                "button:has-text('Барај')",
                "a:has-text('Барај')",
                "button:has-text('Прикажи')",
                "a:has-text('Прикажи')",
                "button:has-text('Search')",
                "a:has-text('Search')",
                "button:has-text('Show')",
                "a:has-text('Show')"
        );

        Locator buttons = page.locator(selector);

        for (int i = 0; i < buttons.count(); i++) {
            try {
                Locator button = buttons.nth(i);

                if (!button.isVisible()) {
                    continue;
                }

                button.click();
                page.waitForLoadState(LoadState.NETWORKIDLE);
                page.waitForTimeout(2_000);
                return;
            } catch (Exception ignored) {
            }
        }
    }

    private String firstHref(Locator row) {
        try {
            Locator links = row.locator("a[href]");

            if (links.count() == 0) {
                return null;
            }

            return links.first().getAttribute("href");
        } catch (Exception ignored) {
            return null;
        }
    }

    private String absoluteUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        if (url.startsWith("http")) {
            return url;
        }

        if (url.startsWith("#")) {
            return baseUrl + url.substring(1);
        }

        try {
            URI base = URI.create("https://www.e-nabavki.gov.mk");
            return base.resolve(url).toString();
        } catch (Exception ignored) {
            return url;
        }
    }

    private boolean isCalendarTable(List<String> headers, String tableText) {
        String combined = (String.join(" ", headers) + " " + tableText).toLowerCase();

        boolean hasWeekDays =
                combined.contains("sun")
                        && combined.contains("mon")
                        && combined.contains("tue")
                        && combined.contains("wed");

        boolean hasEnglishMonth =
                combined.contains("january")
                        || combined.contains("february")
                        || combined.contains("march")
                        || combined.contains("april")
                        || combined.contains("may")
                        || combined.contains("june")
                        || combined.contains("july")
                        || combined.contains("august")
                        || combined.contains("september")
                        || combined.contains("october")
                        || combined.contains("november")
                        || combined.contains("december");

        boolean hasMacedonianMonth =
                combined.contains("јануари")
                        || combined.contains("февруари")
                        || combined.contains("март")
                        || combined.contains("април")
                        || combined.contains("мај")
                        || combined.contains("јуни")
                        || combined.contains("јули")
                        || combined.contains("август")
                        || combined.contains("септември")
                        || combined.contains("октомври")
                        || combined.contains("ноември")
                        || combined.contains("декември");

        return hasWeekDays || hasEnglishMonth || hasMacedonianMonth;
    }

    private boolean looksLikeProcurementTable(List<String> headers, String tableText) {
        String combined = (String.join(" ", headers) + " " + tableText).toLowerCase();

        boolean hasProcurementWords =
                combined.contains("договор")
                        || combined.contains("оглас")
                        || combined.contains("набав")
                        || combined.contains("постап")
                        || combined.contains("економски")
                        || combined.contains("оператор")
                        || combined.contains("договорен орган")
                        || combined.contains("предмет")
                        || combined.contains("носител")
                        || combined.contains("избран")
                        || combined.contains("contract")
                        || combined.contains("notice")
                        || combined.contains("supplier")
                        || combined.contains("institution")
                        || combined.contains("operator")
                        || combined.contains("procedure");

        boolean hasEnoughStructure = headers.size() >= 3 && tableText.length() > 50;

        return hasProcurementWords || hasEnoughStructure;
    }

    private boolean isCalendarRow(Map<String, String> fields) {
        String combined = (fields.keySet() + " " + fields.values()).toLowerCase();

        return combined.contains("sun")
                && combined.contains("mon")
                && combined.contains("tue")
                && combined.contains("wed");
    }

    private boolean isEmptyRow(Map<String, String> fields) {
        if (fields == null || fields.isEmpty()) {
            return true;
        }

        for (String value : fields.values()) {
            if (value != null && !value.trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }
}