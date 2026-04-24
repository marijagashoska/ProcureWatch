package com.procurewatchbackend.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class MojibakeFixer {

    private static final Charset WINDOWS_1252 = Charset.forName("Windows-1252");

    private MojibakeFixer() {
    }

    public static String fix(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String cleaned = value
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();

        String isoFixed = decodeAsUtf8(cleaned, StandardCharsets.ISO_8859_1);
        String winFixed = decodeAsUtf8(cleaned, WINDOWS_1252);

        return best(cleaned, isoFixed, winFixed);
    }

    private static String decodeAsUtf8(String value, Charset wrongCharset) {
        try {
            return new String(value.getBytes(wrongCharset), StandardCharsets.UTF_8)
                    .replace('\u00A0', ' ')
                    .replaceAll("\\s+", " ")
                    .trim();
        } catch (Exception e) {
            return value;
        }
    }

    private static String best(String original, String a, String b) {
        String best = original;

        if (score(a) > score(best)) {
            best = a;
        }

        if (score(b) > score(best)) {
            best = b;
        }

        return best;
    }

    private static int score(String value) {
        if (value == null) {
            return Integer.MIN_VALUE;
        }

        int score = 0;

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            if (c >= '\u0400' && c <= '\u04FF') {
                score += 5; // Cyrillic
            }

            if (c == 'Ð' || c == 'Ñ' || c == 'Â' || c == 'Ã' || c == '\uFFFD') {
                score -= 10; // broken encoding markers
            }

            if (Character.isLetterOrDigit(c)) {
                score += 1;
            }
        }

        return score;
    }
}