package com.procurewatchbackend.util;

public class CsvExportUtil {

    private CsvExportUtil() {
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }

        String escaped = value.replace("\"", "\"\"");

        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }

        return escaped;
    }
}