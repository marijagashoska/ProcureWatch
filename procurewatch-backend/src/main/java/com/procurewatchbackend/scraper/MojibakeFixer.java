package com.procurewatchbackend.scraper;

public final class MojibakeFixer {

    private MojibakeFixer() {
    }

    public static String fix(String value) {
        if (value == null) {
            return null;
        }

        return value
                .replace("\u00A0", " ")
                .replace("\uFEFF", "")
                .replaceAll("[\\t\\r\\n]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}