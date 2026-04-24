package com.procurewatchbackend.scraper;

import com.procurewatchbackend.util.MojibakeFixer;
import com.procurewatchbackend.util.TextNormalizer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ScrapedRow(Map<String, String> fields, String sourceUrl) {

    public ScrapedRow {
        Map<String, String> fixedFields = new LinkedHashMap<>();

        if (fields != null) {
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                String fixedKey = MojibakeFixer.fix(entry.getKey());
                String fixedValue = MojibakeFixer.fix(entry.getValue());

                fixedFields.put(fixedKey, fixedValue);
            }
        }

        fields = Collections.unmodifiableMap(fixedFields);
        sourceUrl = MojibakeFixer.fix(sourceUrl);
    }

    public String get(String... possibleNames) {
        if (possibleNames == null || possibleNames.length == 0) {
            return null;
        }

        for (String possibleName : possibleNames) {
            String fixedPossibleName = MojibakeFixer.fix(possibleName);
            String direct = fields.get(fixedPossibleName);

            if (hasText(direct)) {
                return direct.trim();
            }
        }

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String key = TextNormalizer.normalizeKey(MojibakeFixer.fix(entry.getKey()));

            for (String possibleName : possibleNames) {
                String wanted = TextNormalizer.normalizeKey(MojibakeFixer.fix(possibleName));

                if (key.equals(wanted) || key.contains(wanted) || wanted.contains(key)) {
                    String value = MojibakeFixer.fix(entry.getValue());
                    return hasText(value) ? value.trim() : null;
                }
            }
        }

        return null;
    }

    public ScrapedRow with(String key, String value) {
        Map<String, String> copy = new LinkedHashMap<>(fields);

        if (hasText(key) && hasText(value)) {
            copy.put(MojibakeFixer.fix(key), MojibakeFixer.fix(value));
        }

        return new ScrapedRow(copy, sourceUrl);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}