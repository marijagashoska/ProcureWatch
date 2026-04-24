package com.procurewatchbackend.util;

import java.text.Normalizer;
import java.util.Locale;

public final class TextNormalizer {

    private TextNormalizer() {
    }

    public static String normalizeName(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace('\u00A0', ' ')
                .replaceAll("[“”„\"]", "")
                .replaceAll("\\s+", " ")
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace("ЈАВНА ЗДРАВСТВЕНА УСТАНОВА", "ЈЗУ")
                .replace("ЈАВНО ПРЕТПРИЈАТИЕ", "ЈП");
    }

    public static String normalizeKey(String value) {
        if (value == null) {
            return "";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .replace('\u00A0', ' ')
                .replaceAll("[^\\p{L}\\p{Nd}]+", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    public static String safe(String value) {
        return value == null ? null : value.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }

    public static String firstPart(String value, int maxLength) {
        String safe = safe(value);
        if (safe == null || safe.length() <= maxLength) {
            return safe;
        }
        return safe.substring(0, maxLength).trim();
    }
}