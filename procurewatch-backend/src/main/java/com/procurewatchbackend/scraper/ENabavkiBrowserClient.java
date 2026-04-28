package com.procurewatchbackend.scraper;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ENabavkiBrowserClient {

    private static final String BASE_URL = "https://www.e-nabavki.gov.mk/PublicAccess/home.aspx";

    @Value("${procurewatch.scraper.headless:true}")
    private boolean headless;

    @Value("${procurewatch.scraper.navigation-timeout-ms:45000}")
    private int navigationTimeoutMs;

    public List<ScrapedRow> scrapeList(String route, int maxPages) {
        List<ScrapedRow> allRows = new ArrayList<>();

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions().setHeadless(headless)
             )) {

            Page page = browser.newPage();
            configurePage(page);

            page.navigate(toRouteUrl(route));
            waitForUsefulContent(page);

            int pageNumber = 1;

            while (true) {
                List<ScrapedRow> currentRows = readRowsFromTables(page);
                allRows.addAll(currentRows);

                if (maxPages > 0 && pageNumber >= maxPages) {
                    break;
                }

                boolean moved = clickNextPage(page);

                if (!moved) {
                    break;
                }

                pageNumber++;
            }
        }

        return allRows;
    }

    public List<ScrapedRow> scrapeListWithDetails(String route, int maxPages, int maxDetails) {
        List<ScrapedRow> listRows = scrapeList(route, maxPages);
        List<ScrapedRow> enrichedRows = new ArrayList<>();

        int openedDetails = 0;

        for (ScrapedRow listRow : listRows) {
            String detailUrl = listRow.sourceUrl();

            if (detailUrl == null || detailUrl.isBlank()) {
                enrichedRows.add(listRow);
                continue;
            }

            if (maxDetails > 0 && openedDetails >= maxDetails) {
                enrichedRows.add(listRow);
                continue;
            }

            try {
                ScrapedRow detailRow = scrapeDetail(detailUrl);
                enrichedRows.add(listRow.merge(detailRow));
                openedDetails++;
            } catch (Exception ex) {
                enrichedRows.add(listRow.with("_detailScrapeError", ex.getMessage()));
            }
        }

        return enrichedRows;
    }

    public List<ScrapedRow> scrapeAnnualPlanItemDetails(List<ScrapedRow> annualPlanRows, int maxDetails) {
        List<ScrapedRow> planItemRows = new ArrayList<>();

        int openedDetails = 0;

        for (ScrapedRow annualPlanRow : annualPlanRows) {
            String detailUrl = annualPlanRow.sourceUrl();

            if (detailUrl == null || detailUrl.isBlank()) {
                continue;
            }

            if (maxDetails > 0 && openedDetails >= maxDetails) {
                break;
            }

            try {
                planItemRows.addAll(scrapeAnnualPlanDetailPage(detailUrl, annualPlanRow));
                openedDetails++;
            } catch (Exception ex) {
                planItemRows.add(annualPlanRow.with("_detailScrapeError", ex.getMessage()));
            }
        }

        return planItemRows;
    }

    private List<ScrapedRow> scrapeAnnualPlanDetailPage(String url, ScrapedRow annualPlanRow) {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions().setHeadless(headless)
             )) {

            Page page = browser.newPage();
            configurePage(page);

            page.navigate(toAbsoluteUrl(url));
            waitForUsefulContent(page);
            expandAllSections(page);

            Map<String, String> institutionDetails = readInstitutionBlock(page);

            // IMPORTANT: use plan-item-specific parser, not generic readRowsFromTables(page)
            List<ScrapedRow> itemRows = readAnnualPlanItemRows(page);

            List<ScrapedRow> result = new ArrayList<>();

            for (ScrapedRow itemRow : itemRows) {
                Map<String, String> merged = new LinkedHashMap<>(itemRow.fields());

                String institutionName = firstText(
                        institutionDetails.get("_institutionOfficialName"),
                        institutionDetails.get("Назив на договорниот орган"),
                        annualPlanRow.get(
                                "Назив на договорниот орган",
                                "Договорен орган",
                                "Институција",
                                "Contracting authority",
                                "Institution",
                                "institution"
                        )
                );

                putIfText(merged, "_parentInstitution", institutionName);
                putIfText(merged, "_parentYear", annualPlanRow.get("Година", "План за година", "year"));
                putIfText(merged, "_parentPublicationDate", annualPlanRow.get("Датум на објава", "Датум на објавување", "publicationDate"));
                putIfText(merged, "_parentSourceUrl", page.url());

                putIfText(merged, "_institutionOfficialName", institutionDetails.get("_institutionOfficialName"));
                putIfText(merged, "_institutionAddress", institutionDetails.get("_institutionAddress"));
                putIfText(merged, "_institutionCity", institutionDetails.get("_institutionCity"));
                putIfText(merged, "_institutionPostalCode", institutionDetails.get("_institutionPostalCode"));
                putIfText(merged, "_institutionWebsite", institutionDetails.get("_institutionWebsite"));
                putIfText(merged, "_institutionContact", institutionDetails.get("_institutionContact"));
                putIfText(merged, "_institutionCategory", institutionDetails.get("_institutionCategory"));

                putIfText(merged, "Назив на договорниот орган", institutionDetails.get("Назив на договорниот орган"));
                putIfText(merged, "Адреса", institutionDetails.get("Адреса"));
                putIfText(merged, "Град", institutionDetails.get("Град"));
                putIfText(merged, "Поштенски код", institutionDetails.get("Поштенски код"));
                putIfText(merged, "Поштенски број", institutionDetails.get("Поштенски број"));
                putIfText(merged, "Категорија", institutionDetails.get("Категорија"));

                result.add(new ScrapedRow(merged, page.url()));
            }

            return result;
        }
    }
    @SuppressWarnings("unchecked")
    private Map<String, String> readAnnualPlanInstitutionDetails(Page page) {
        Object result = page.evaluate("""
        () => {
          const out = {};

          const clean = (s) => (s || '')
            .replace(/\\u00A0/g, ' ')
            .replace(/\\s+/g, ' ')
            .trim();

          const rawLines = (document.body.innerText || '')
            .split(/\\n+/)
            .map(clean)
            .filter(Boolean);

          const put = (key, value) => {
            key = clean(key);
            value = clean(value);

            if (!key || !value) return;
            out[key] = value;
          };

          const findAfter = (regex) => {
            for (const line of rawLines) {
              const match = line.match(regex);
              if (match && match[1]) {
                return clean(match[1]);
              }
            }
            return null;
          };

          const name = findAfter(/1\\.1\\.1\\)\\s*Назив на договорниот орган:\\s*(.+)$/i);
          const address = findAfter(/1\\.1\\.2\\)\\s*Адреса:\\s*(.+)$/i);
          const cityPostal = findAfter(/1\\.1\\.3\\)\\s*Град и поштенски код:\\s*(.+)$/i);
          const website = findAfter(/1\\.1\\.4\\)\\s*Интернет адреса:\\s*(.+)$/i);
          const contact = findAfter(/1\\.1\\.5\\)\\s*Лице за контакт:\\s*(.+)$/i);

          put('Назив на договорниот орган', name);
          put('Адреса', address);
          put('Интернет адреса', website);
          put('Лице за контакт', contact);

          if (cityPostal) {
            const match = cityPostal.match(/^(.+?)\\s+(\\d{4})$/);
            if (match) {
              put('Град', match[1]);
              put('Поштенски код', match[2]);
            } else {
              put('Град', cityPostal);
            }
          }

          const categoryIndex = rawLines.findIndex(line =>
            line.includes('1.2)') && line.includes('Категорија')
          );

          if (categoryIndex >= 0) {
            const category = rawLines[categoryIndex + 1];
            if (category && !category.startsWith('ДЕЛ II')) {
              put('Категорија', category);
            }
          }

          return out;
        }
    """);

        Map<String, String> fields = new LinkedHashMap<>();

        if (!(result instanceof Map<?, ?> map)) {
            return fields;
        }

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }

            fields.put(
                    com.procurewatchbackend.scraper.MojibakeFixer.fix(entry.getKey().toString()),
                    com.procurewatchbackend.scraper.MojibakeFixer.fix(entry.getValue().toString())
            );
        }

        return fields;
    }

    private String firstText(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }

        return null;
    }

    private void putIfText(Map<String, String> map, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            map.put(key, value.trim());
        }
    }

    public ScrapedRow scrapeDetail(String url) {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions().setHeadless(headless)
             )) {

            Page page = browser.newPage();
            configurePage(page);

            page.navigate(toAbsoluteUrl(url));
            waitForUsefulContent(page);
            expandAllSections(page);

            Map<String, String> fields = new LinkedHashMap<>();

            for (ScrapedRow tableRow : readRowsFromTables(page)) {
                fields.putAll(tableRow.fields());
            }

            fields.putAll(readKeyValueFields(page));
            fields.put("_pageText", safeText(page, "body"));

            return new ScrapedRow(fields, page.url());
        }
    }

    private List<ScrapedRow> scrapeRowsFromDetailPage(String url) {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions().setHeadless(headless)
             )) {

            Page page = browser.newPage();
            configurePage(page);

            page.navigate(toAbsoluteUrl(url));
            waitForUsefulContent(page);
            expandAllSections(page);

            List<ScrapedRow> rows = readRowsFromTables(page);

            if (rows.isEmpty()) {
                Map<String, String> fields = readKeyValueFields(page);
                rows.add(new ScrapedRow(fields, page.url()));
            }

            return rows;
        }
    }

    private void configurePage(Page page) {
        page.setDefaultTimeout(navigationTimeoutMs);
        page.setDefaultNavigationTimeout(navigationTimeoutMs);
    }

    private void waitForUsefulContent(Page page) {
        try {
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        } catch (Exception ignored) {
        }

        try {
            page.waitForLoadState(LoadState.NETWORKIDLE);
        } catch (Exception ignored) {
        }

        page.waitForTimeout(1500);

        closeDatePickerPopups(page);
        clickSearchButtonIfPresent(page);
        closeDatePickerPopups(page);

        try {
            page.locator("table, [role='grid'], .table, body")
                    .first()
                    .waitFor(new Locator.WaitForOptions().setTimeout(15_000));
        } catch (Exception ignored) {
        }

        page.waitForTimeout(1000);
    }

    private void closeDatePickerPopups(Page page) {
        try {
            page.keyboard().press("Escape");
            page.waitForTimeout(300);
        } catch (Exception ignored) {
        }

        try {
            page.mouse().click(5, 5);
            page.waitForTimeout(300);
        } catch (Exception ignored) {
        }

        try {
            page.evaluate("""
            () => {
              document.querySelectorAll(
                '.datepicker, .ui-datepicker, .datepicker-dropdown, .bootstrap-datetimepicker-widget, .flatpickr-calendar, .mat-datepicker-content'
              ).forEach(el => {
                el.style.display = 'none';
                el.setAttribute('aria-hidden', 'true');
              });

              if (document.activeElement) {
                document.activeElement.blur();
              }
            }
        """);
        } catch (Exception ignored) {
        }
    }

    private void clickSearchButtonIfPresent(Page page) {
        String selector = String.join(",",
                "button:has-text('Пребарај')",
                "a:has-text('Пребарај')",
                "input[value='Пребарај']",
                "button:has-text('Барај')",
                "a:has-text('Барај')",
                "input[value='Барај']",
                "button:has-text('Search')",
                "a:has-text('Search')",
                "input[value='Search']"
        );

        try {
            Locator buttons = page.locator(selector);
            int count = Math.min(buttons.count(), 5);

            for (int i = 0; i < count; i++) {
                try {
                    Locator button = buttons.nth(i);

                    if (button.isVisible()) {
                        button.click(new Locator.ClickOptions().setTimeout(3000));
                        page.waitForLoadState(LoadState.NETWORKIDLE);
                        page.waitForTimeout(1500);
                        return;
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private List<ScrapedRow> readRowsFromTables(Page page) {
        Object result = page.evaluate("""
        () => {
          const clean = (s) => (s || '')
            .replace(/\\u00A0/g, ' ')
            .replace(/\\s+/g, ' ')
            .trim();

          const absolute = (href) => {
            if (!href) return null;
            try {
              return new URL(href, window.location.href).href;
            } catch (e) {
              return href;
            }
          };

          const isCalendarTable = (table) => {
            const text = clean(table.innerText);
            const lower = text.toLowerCase();

            if (table.closest(
              '.datepicker, .ui-datepicker, .datepicker-dropdown, .bootstrap-datetimepicker-widget, .flatpickr-calendar, .mat-datepicker-content'
            )) {
              return true;
            }

            const hasWeekDays =
              text.includes('Sun') &&
              text.includes('Mon') &&
              text.includes('Tue') &&
              text.includes('Wed') &&
              text.includes('Thu') &&
              text.includes('Fri') &&
              text.includes('Sat');

            const hasMonth =
              /january|february|march|april|may|june|july|august|september|october|november|december/i.test(text);

            if (hasWeekDays && hasMonth) {
              return true;
            }

            if (lower.includes('today') && hasWeekDays) {
              return true;
            }

            return false;
          };

          const looksLikeProcurementTable = (headers, tableText) => {
            const combined = clean([...headers, tableText].join(' ')).toLowerCase();

            const markers = [
              'број на оглас',
              'бр. на оглас',
              'оглас',
              'договорен орган',
              'институција',
              'предмет',
              'постапка',
              'договор',
              'вредност',
              'носител',
              'економски оператор',
              'датум',
              'notice',
              'contract',
              'supplier'
            ];

            return markers.some(marker => combined.includes(marker));
          };

          const bestHref = (tr) => {
            const links = [...tr.querySelectorAll('a[href]')];

            if (!links.length) return null;

            const preferred = links.find(a => {
              const href = a.getAttribute('href') || '';
              const text = clean(a.innerText).toLowerCase();

              return href.includes('dossie') ||
                     href.includes('contract') ||
                     href.includes('contracts') ||
                     href.includes('notice') ||
                     href.includes('notices') ||
                     href.includes('decision') ||
                     href.includes('tender') ||
                     text.includes('детали') ||
                     text.includes('прикажи') ||
                     text.includes('отвори') ||
                     text.includes('details');
            });

            return absolute((preferred || links[0]).getAttribute('href'));
          };

          const all = [];

          document.querySelectorAll('table').forEach((table, tableIndex) => {
            if (isCalendarTable(table)) {
              return;
            }

            const tableText = clean(table.innerText);

            if (!tableText) {
              return;
            }

            let headers = [...table.querySelectorAll('thead tr th')]
              .map(th => clean(th.innerText))
              .filter(Boolean);

            if (!headers.length) {
              const firstHeaderRow = [...table.querySelectorAll('tr')]
                .find(tr => tr.querySelectorAll('th').length > 0);

              if (firstHeaderRow) {
                headers = [...firstHeaderRow.querySelectorAll('th')]
                  .map(th => clean(th.innerText))
                  .filter(Boolean);
              }
            }

            if (!looksLikeProcurementTable(headers, tableText)) {
              return;
            }

            const bodyRows = [...table.querySelectorAll('tbody tr')];

            const usableRows = bodyRows.length
              ? bodyRows
              : [...table.querySelectorAll('tr')].filter(tr => tr.querySelectorAll('td').length > 0);

            usableRows.forEach((tr, rowIndex) => {
              const cells = [...tr.querySelectorAll('td')]
                .map(td => clean(td.innerText));

              if (!cells.length) {
                return;
              }

              const nonEmptyCells = cells.filter(Boolean);

              if (!nonEmptyCells.length) {
                return;
              }

              const rowText = clean(nonEmptyCells.join(' '));

              if (!looksLikeProcurementTable(headers, rowText)) {
                return;
              }

              const obj = {};

              cells.forEach((cell, index) => {
                const header = headers[index] || `column_${index + 1}`;
                obj[header] = cell;
              });

              obj['_tableIndex'] = String(tableIndex);
              obj['_rowIndex'] = String(rowIndex);

              const href = bestHref(tr);

              if (href) {
                obj['_sourceUrl'] = href;
              }

              all.push(obj);
            });
          });

          return all;
        }
    """);

        List<ScrapedRow> rows = new ArrayList<>();

        if (!(result instanceof List<?> list)) {
            return rows;
        }

        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }

            Map<String, String> fields = new LinkedHashMap<>();
            String sourceUrl = page.url();

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) {
                    continue;
                }

                String key = entry.getKey().toString();
                String value = com.procurewatchbackend.scraper.MojibakeFixer.fix(entry.getValue().toString());

                if ("_sourceUrl".equals(key)) {
                    sourceUrl = value;
                } else {
                    fields.put(com.procurewatchbackend.scraper.MojibakeFixer.fix(key), value);
                }
            }

            rows.add(new ScrapedRow(fields, sourceUrl));
        }

        return rows;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> readKeyValueFields(Page page) {
        Object result = page.evaluate("""
            () => {
              const out = {};

              const clean = (s) => (s || '')
                .replace(/\\u00A0/g, ' ')
                .replace(/\\s+/g, ' ')
                .trim();

              const put = (k, v) => {
                k = clean(k);
                v = clean(v);

                if (!k || !v || k === v) return;
                if (k.length > 160) return;
                if (v.length > 12000) v = v.substring(0, 12000);

                out[k] = v;
              };

              document.querySelectorAll('tr').forEach(tr => {
                const cells = [...tr.querySelectorAll('th,td')]
                  .map(x => clean(x.innerText))
                  .filter(Boolean);

                if (cells.length === 2) {
                  put(cells[0], cells[1]);
                }

                if (cells.length > 2) {
                  for (let i = 0; i < cells.length - 1; i += 2) {
                    put(cells[i], cells[i + 1]);
                  }
                }
              });

              document.querySelectorAll('dl').forEach(dl => {
                const children = [...dl.children];

                for (let i = 0; i < children.length - 1; i++) {
                  if (children[i].tagName.toLowerCase() === 'dt') {
                    put(children[i].innerText, children[i + 1].innerText);
                  }
                }
              });

              document.querySelectorAll('label').forEach(label => {
                const key = clean(label.innerText);
                const parent = label.closest('.form-group, .row, div');

                if (!parent) return;

                let value = clean(parent.innerText);
                value = clean(value.replace(key, ''));

                put(key, value);
              });

              document.querySelectorAll('[class*=label], [class*=title], [class*=name]').forEach(el => {
                const parent = el.closest('.row, .form-group, div');

                if (!parent) return;

                const key = clean(el.innerText);
                const value = clean(parent.innerText).replace(key, '').trim();

                put(key, value);
              });

              return out;
            }
        """);

        Map<String, String> fields = new LinkedHashMap<>();

        if (!(result instanceof Map<?, ?> map)) {
            return fields;
        }

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }

            fields.put(
                    com.procurewatchbackend.scraper.MojibakeFixer.fix(entry.getKey().toString()),
                    com.procurewatchbackend.scraper.MojibakeFixer.fix(entry.getValue().toString())
            );
        }

        return fields;
    }

    @SuppressWarnings("unchecked")
    public List<String> debugTableTexts(String route) {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions().setHeadless(headless)
             )) {

            Page page = browser.newPage();
            configurePage(page);

            page.navigate(toRouteUrl(route));
            waitForUsefulContent(page);

            Object result = page.evaluate("""
            () => [...document.querySelectorAll('table')].map((table, index) => {
              const text = (table.innerText || '')
                .replace(/\\u00A0/g, ' ')
                .replace(/\\s+/g, ' ')
                .trim();

              return `TABLE ${index}: ${text.substring(0, 1000)}`;
            })
        """);

            if (result instanceof List<?> list) {
                return list.stream()
                        .map(String::valueOf)
                        .toList();
            }

            return List.of();
        }
    }

    private void expandAllSections(Page page) {
        String selector = String.join(",",
                "button:has-text('Детали')",
                "a:has-text('Детали')",
                "button:has-text('Повеќе')",
                "a:has-text('Повеќе')",
                "button:has-text('Прикажи')",
                "a:has-text('Прикажи')",
                "button:has-text('Отвори')",
                "a:has-text('Отвори')",
                ".collapsed",
                "[aria-expanded='false']"
        );

        Locator controls = page.locator(selector);

        int count;

        try {
            count = Math.min(controls.count(), 30);
        } catch (Exception ex) {
            return;
        }

        for (int i = 0; i < count; i++) {
            try {
                Locator control = controls.nth(i);

                if (control.isVisible()) {
                    control.click(new Locator.ClickOptions().setTimeout(2_000));
                    page.waitForTimeout(300);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private boolean clickNextPage(Page page) {
        String before = visibleTableSignature(page);

        String selector = String.join(",",
                "a:has-text('Следна')",
                "button:has-text('Следна')",
                "a:has-text('Напред')",
                "button:has-text('Напред')",
                "a:has-text('Next')",
                "button:has-text('Next')",
                "a:has-text('›')",
                "button:has-text('›')",
                "a:has-text('»')",
                "button:has-text('»')",
                "li.pagination-next:not(.disabled) a",
                ".pagination-next:not(.disabled) a",
                "[aria-label*='Next']",
                "[aria-label*='Следна']"
        );

        Locator candidates = page.locator(selector);

        int count;

        try {
            count = Math.min(candidates.count(), 20);
        } catch (Exception ex) {
            return false;
        }

        for (int i = 0; i < count; i++) {
            try {
                Locator candidate = candidates.nth(i);

                if (!candidate.isVisible()) {
                    continue;
                }

                String className = candidate.getAttribute("class");
                String ariaDisabled = candidate.getAttribute("aria-disabled");

                if ("true".equalsIgnoreCase(ariaDisabled)) {
                    continue;
                }

                if (className != null && className.toLowerCase().contains("disabled")) {
                    continue;
                }

                candidate.click(new Locator.ClickOptions().setTimeout(5_000));
                waitForUsefulContent(page);

                String after = visibleTableSignature(page);

                return !before.equals(after);
            } catch (Exception ignored) {
            }
        }

        return false;
    }

    private String visibleTableSignature(Page page) {
        try {
            return page.locator("table").allInnerTexts().toString();
        } catch (Exception ex) {
            return page.content();
        }
    }

    private String safeText(Page page, String selector) {
        try {
            return com.procurewatchbackend.scraper.MojibakeFixer.fix(page.locator(selector).first().innerText());
        } catch (Exception ex) {
            return "";
        }
    }

    private String toRouteUrl(String route) {
        if (route == null || route.isBlank()) {
            return BASE_URL;
        }

        String trimmed = route.trim();

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }

        if (trimmed.startsWith("#/")) {
            return BASE_URL + trimmed;
        }

        if (trimmed.startsWith("/")) {
            return BASE_URL + "#" + trimmed;
        }

        return BASE_URL + "#/" + trimmed.replaceFirst("^#+/?", "");
    }

    private String toAbsoluteUrl(String url) {
        if (url == null || url.isBlank()) {
            return BASE_URL;
        }

        String trimmed = url.trim();

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }

        if (trimmed.startsWith("#/")) {
            return BASE_URL + trimmed;
        }

        if (trimmed.startsWith("/PublicAccess")) {
            return "https://www.e-nabavki.gov.mk" + trimmed;
        }

        if (trimmed.startsWith("/")) {
            return "https://www.e-nabavki.gov.mk" + trimmed;
        }

        return BASE_URL + "#/" + trimmed.replaceFirst("^#+/?", "");
    }


    @SuppressWarnings("unchecked")
    private Map<String, String> readInstitutionBlock(Page page) {
        Object result = page.evaluate("""
        () => {
          const out = {};

          const clean = (s) => (s || '')
            .replace(/\\u00A0/g, ' ')
            .replace(/\\s+/g, ' ')
            .trim();

          const lines = (document.body.innerText || '')
            .split(/\\n+/)
            .map(clean)
            .filter(Boolean);

          const put = (key, value) => {
            key = clean(key);
            value = clean(value);

            if (key && value) {
              out[key] = value;
            }
          };

          const removePrefix = (line, codeRegex, labelRegex) => {
            let value = clean(line);
            value = value.replace(codeRegex, '').trim();

            if (labelRegex) {
              value = value.replace(labelRegex, '').trim();
            }

            value = value.replace(/^[:\\-–]+/, '').trim();
            return clean(value);
          };

          const findSingle = (codeRegex, labelRegex) => {
            for (let i = 0; i < lines.length; i++) {
              const line = lines[i];

              if (!codeRegex.test(line)) {
                continue;
              }

              const sameLineValue = removePrefix(line, codeRegex, labelRegex);

              if (sameLineValue) {
                return sameLineValue;
              }

              if (i + 1 < lines.length) {
                return clean(lines[i + 1]);
              }
            }

            return null;
          };

          const findCategory = () => {
            const codeRegex = /^(?:I|1)\\.2\\)\\s*/i;
            const labelRegex = /^Категорија на договорен орган.*?:\\s*/i;

            for (let i = 0; i < lines.length; i++) {
              const line = lines[i];

              if (!codeRegex.test(line)) {
                continue;
              }

              const parts = [];

              const sameLineValue = removePrefix(line, codeRegex, labelRegex);

              if (sameLineValue) {
                parts.push(sameLineValue);
              }

              for (let j = i + 1; j < lines.length; j++) {
                const next = lines[j];

                if (/^ДЕЛ\\s+II/i.test(next)) break;
                if (/^II\\./i.test(next)) break;
                if (/^(?:I|1)\\.\\d/i.test(next)) break;

                parts.push(next);
              }

              return clean(parts.join(' '));
            }

            return null;
          };

          const name = findSingle(
            /^(?:I|1)\\.1\\.1\\)\\s*/i,
            /^Назив на договорниот орган\\s*:?\\s*/i
          );

          const address = findSingle(
            /^(?:I|1)\\.1\\.2\\)\\s*/i,
            /^Адреса\\s*:?\\s*/i
          );

          const cityPostal = findSingle(
            /^(?:I|1)\\.1\\.3\\)\\s*/i,
            /^Град и поштенски код\\s*:?\\s*/i
          );

          const website = findSingle(
            /^(?:I|1)\\.1\\.4\\)\\s*/i,
            /^Интернет адреса\\s*:?\\s*/i
          );

          const contact = findSingle(
            /^(?:I|1)\\.1\\.5\\)\\s*/i,
            /^Лице за контакт\\s*:?\\s*/i
          );

          const category = findCategory();

          put('_institutionOfficialName', name);
          put('Назив на договорниот орган', name);

          put('_institutionAddress', address);
          put('Адреса', address);

          put('_institutionWebsite', website);
          put('Интернет адреса', website);

          put('_institutionContact', contact);
          put('Лице за контакт', contact);

          put('_institutionCategory', category);
          put('Категорија', category);

          if (cityPostal) {
            const match = cityPostal.match(/^(.+?)\\s+(\\d{4,5})$/);

            if (match) {
              put('_institutionCity', match[1]);
              put('Град', match[1]);

              put('_institutionPostalCode', match[2]);
              put('Поштенски код', match[2]);
              put('Поштенски број', match[2]);
            } else {
              put('_institutionCity', cityPostal);
              put('Град', cityPostal);
            }
          }

          return out;
        }
    """);

        Map<String, String> fields = new LinkedHashMap<>();

        if (!(result instanceof Map<?, ?> map)) {
            return fields;
        }

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }

            fields.put(
                    com.procurewatchbackend.scraper.MojibakeFixer.fix(entry.getKey().toString()),
                    com.procurewatchbackend.scraper.MojibakeFixer.fix(entry.getValue().toString())
            );
        }

        return fields;
    }
    @SuppressWarnings("unchecked")
    private List<ScrapedRow> readAnnualPlanItemRows(Page page) {
        Object result = page.evaluate("""
        () => {
          const clean = (s) => (s || '')
            .replace(/\\u00A0/g, ' ')
            .replace(/\\s+/g, ' ')
            .trim();

          const normalize = (s) => clean(s).toLowerCase();

          const tableIsPlanItemsTable = (headers) => {
            const joined = normalize(headers.join(' '));

            return joined.includes('предмет')
              && (
                joined.includes('зпјн') ||
                joined.includes('зјн') ||
                joined.includes('cpv')
              )
              && (
                joined.includes('вид на постапка') ||
                joined.includes('постапка')
              );
          };

          const detectHasNotice = (cell) => {
            if (!cell) return false;

            const text = normalize(cell.innerText || cell.textContent || '');
            const html = normalize(cell.innerHTML || '');

            if (
              text === 'да' ||
              text === 'yes' ||
              text === 'true' ||
              text.includes('има')
            ) {
              return true;
            }

            if (
              html.includes('glyphicon') ||
              html.includes('fa-') ||
              html.includes('icon') ||
              html.includes('ng-click') ||
              html.includes('data-ng-click') ||
              html.includes('href') ||
              html.includes('dossie') ||
              html.includes('оглас')
            ) {
              return true;
            }

            const clickable = cell.querySelector(
              'a, button, i, svg, img, span.glyphicon, span[class*="icon"], span[class*="fa"], [ng-click], [data-ng-click], [onclick]'
            );

            return !!clickable;
          };

          const rows = [];
          const tables = Array.from(document.querySelectorAll('table'));

          for (let tableIndex = 0; tableIndex < tables.length; tableIndex++) {
            const table = tables[tableIndex];

            const headerCells = Array.from(table.querySelectorAll('thead th'));

            let headers = headerCells.map(th => clean(th.innerText || th.textContent));

            if (headers.length === 0) {
              const firstRowTh = Array.from(table.querySelectorAll('tr:first-child th'));
              headers = firstRowTh.map(th => clean(th.innerText || th.textContent));
            }

            if (headers.length === 0) {
              const firstRowCells = Array.from(table.querySelectorAll('tr:first-child td'));
              headers = firstRowCells.map(td => clean(td.innerText || td.textContent));
            }

            if (!tableIsPlanItemsTable(headers)) {
              continue;
            }

            const bodyRows = Array.from(table.querySelectorAll('tbody tr'))
              .filter(tr => tr.querySelectorAll('td').length > 0);

            for (let rowIndex = 0; rowIndex < bodyRows.length; rowIndex++) {
              const tr = bodyRows[rowIndex];
              const cells = Array.from(tr.querySelectorAll('td'));

              if (cells.length === 0) {
                continue;
              }

              const row = {};

              for (let cellIndex = 0; cellIndex < cells.length; cellIndex++) {
                const header = headers[cellIndex] || `column_${cellIndex}`;
                const value = clean(cells[cellIndex].innerText || cells[cellIndex].textContent);

                row[header] = value;
              }

              const noticeIndex = headers.findIndex(h => normalize(h).includes('оглас'));
              const noticeCell = noticeIndex >= 0 ? cells[noticeIndex] : cells[cells.length - 1];

              const hasNotice = detectHasNotice(noticeCell);

              row['_hasNotice'] = hasNotice ? 'true' : 'false';
              row['Оглас'] = hasNotice ? 'true' : 'false';
              row['hasNotice'] = hasNotice ? 'true' : 'false';

              row['_tableIndex'] = String(tableIndex);
              row['_rowIndex'] = String(rowIndex);

              rows.push(row);
            }
          }

          return rows;
        }
    """);

        List<ScrapedRow> rows = new ArrayList<>();

        if (!(result instanceof List<?> list)) {
            return rows;
        }

        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }

            Map<String, String> fields = new LinkedHashMap<>();

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) {
                    continue;
                }

                fields.put(
                        com.procurewatchbackend.scraper.MojibakeFixer.fix(entry.getKey().toString()),
                        com.procurewatchbackend.scraper.MojibakeFixer.fix(entry.getValue().toString())
                );
            }

            rows.add(new ScrapedRow(fields, page.url()));
        }

        return rows;
    }



}
