package com.procurewatchbackend.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class ValueParser {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("d.M.yyyy"),
            DateTimeFormatter.ofPattern("dd.M.yyyy"),
            DateTimeFormatter.ofPattern("d.MM.yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ISO_LOCAL_DATE
    );

    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("d.M.yyyy H:mm"),
            DateTimeFormatter.ofPattern("dd.M.yyyy H:mm"),
            DateTimeFormatter.ofPattern("d.MM.yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"),
            DateTimeFormatter.ofPattern("d/M/yyyy H:mm"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
    );

    private ValueParser() {
    }

    public static BigDecimal parseMoney(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String value = raw
                .replace("\u00A0", " ")
                .replaceAll("[^0-9,\\.\\-]", "")
                .trim();

        if (value.isBlank() || value.equals("-")) {
            return null;
        }

        int lastComma = value.lastIndexOf(',');
        int lastDot = value.lastIndexOf('.');

        String normalized;

        if (lastComma >= 0 && lastDot >= 0) {
            if (lastComma > lastDot) {
                normalized = value.replace(".", "").replace(",", ".");
            } else {
                normalized = value.replace(",", "");
            }
        } else if (lastComma >= 0) {
            normalized = value.replace(".", "").replace(",", ".");
        } else {
            normalized = value.replace(",", "");
        }

        try {
            return new BigDecimal(normalized);
        } catch (Exception ex) {
            return null;
        }
    }

    public static Integer parseYear(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String cleaned = raw
                .replace("\u00A0", " ")
                .replaceAll("[^0-9]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleaned.isBlank()) {
            return null;
        }

        for (String part : cleaned.split(" ")) {
            if (part.length() == 4) {
                try {
                    int year = Integer.parseInt(part);
                    if (year >= 2000 && year <= 2100) {
                        return year;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return null;
    }

    public static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String value = cleanDate(raw);

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (Exception ignored) {
            }
        }

        LocalDateTime dateTime = parseDateTime(value);
        return dateTime == null ? null : dateTime.toLocalDate();
    }

    public static LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String value = cleanDate(raw);

        for (DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (Exception ignored) {
            }
        }

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(value, formatter).atStartOfDay();
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    public static String detectCurrency(String raw) {
        if (raw == null || raw.isBlank()) {
            return "MKD";
        }

        String value = raw.toUpperCase();

        if (value.contains("EUR") || value.contains("ЕУР")) {
            return "EUR";
        }

        if (value.contains("USD") || value.contains("УСД")) {
            return "USD";
        }

        if (value.contains("MKD") || value.contains("МКД") || value.contains("ДЕН")) {
            return "MKD";
        }

        return "MKD";
    }

    private static String cleanDate(String raw) {
        return raw
                .replace("\u00A0", " ")
                .replace(",", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}