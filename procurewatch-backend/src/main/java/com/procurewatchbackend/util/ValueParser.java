package com.procurewatchbackend.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ValueParser {

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("d.M.yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
    );

    private static final List<DateTimeFormatter> DATE_TIME_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("d.M.yyyy H:mm"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm"),
            DateTimeFormatter.ofPattern("d.M.yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"),
            DateTimeFormatter.ofPattern("d/M/yyyy H:mm"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy H:mm")
    );

    private ValueParser() {
    }

    public static Integer parseYear(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        Matcher matcher = Pattern.compile("(20\\d{2}|19\\d{2})").matcher(value);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return null;
    }

    public static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String cleaned = cleanDateText(value);

        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                return LocalDate.parse(cleaned, formatter);
            } catch (Exception ignored) {
            }
        }

        for (DateTimeFormatter formatter : DATE_TIME_FORMATS) {
            try {
                return LocalDateTime.parse(cleaned, formatter).toLocalDate();
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    public static LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String cleaned = cleanDateText(value);

        for (DateTimeFormatter formatter : DATE_TIME_FORMATS) {
            try {
                return LocalDateTime.parse(cleaned, formatter);
            } catch (Exception ignored) {
            }
        }

        LocalDate date = parseDate(cleaned);
        return date == null ? null : LocalDateTime.of(date, LocalTime.MIDNIGHT);
    }

    public static BigDecimal parseMoney(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String cleaned = value
                .replace('\u00A0', ' ')
                .replaceAll("[^0-9,.-]", "")
                .trim();

        if (cleaned.isBlank()) {
            return null;
        }

        int lastComma = cleaned.lastIndexOf(',');
        int lastDot = cleaned.lastIndexOf('.');

        if (lastComma > lastDot) {
            cleaned = cleaned.replace(".", "").replace(",", ".");
        } else {
            cleaned = cleaned.replace(",", "");
        }

        try {
            return new BigDecimal(cleaned);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String detectCurrency(String value) {
        if (value == null) {
            return "MKD";
        }

        String upper = value.toUpperCase(Locale.ROOT);

        if (upper.contains("EUR") || upper.contains("ЕУР")) {
            return "EUR";
        }

        if (upper.contains("USD")) {
            return "USD";
        }

        return "MKD";
    }

    private static String cleanDateText(String value) {
        return value
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .replaceAll("(година|год\\.)", "")
                .trim();
    }
}