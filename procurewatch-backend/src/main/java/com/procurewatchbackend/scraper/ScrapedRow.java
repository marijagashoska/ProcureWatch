package com.procurewatchbackend.scraper;

import java.util.LinkedHashMap;
import java.util.Map;

public record ScrapedRow(
        Map<String, String> fields,
        String sourceUrl
) {

    public ScrapedRow {
        if (fields == null) {
            fields = new LinkedHashMap<>();
        }
    }

    public String get(String... aliases) {
        if (aliases == null) {
            return null;
        }

        for (String alias : aliases) {
            if (alias == null || alias.isBlank()) {
                continue;
            }

            String direct = fields.get(alias);
            if (isPresent(direct)) {
                return direct.trim();
            }

            String normalizedAlias = normalizeKey(alias);

            for (Map.Entry<String, String> entry : fields.entrySet()) {
                String normalizedKey = normalizeKey(entry.getKey());

                if (normalizedKey.equals(normalizedAlias)) {
                    String value = entry.getValue();
                    return value == null ? null : value.trim();
                }
            }
        }

        return null;
    }

    public ScrapedRow merge(ScrapedRow other) {
        if (other == null) {
            return this;
        }

        Map<String, String> merged = new LinkedHashMap<>(this.fields);

        for (Map.Entry<String, String> entry : other.fields.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (isPresent(key) && isPresent(value)) {
                merged.put(key.trim(), value.trim());
            }
        }

        String mergedSourceUrl = isPresent(other.sourceUrl)
                ? other.sourceUrl
                : this.sourceUrl;

        return new ScrapedRow(merged, mergedSourceUrl);
    }

    public ScrapedRow with(String key, String value) {
        Map<String, String> updated = new LinkedHashMap<>(this.fields);

        if (isPresent(key) && isPresent(value)) {
            updated.put(key.trim(), value.trim());
        }

        return new ScrapedRow(updated, this.sourceUrl);
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    private static String normalizeKey(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\u00A0", " ")
                .replace(":", "")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
    }
}