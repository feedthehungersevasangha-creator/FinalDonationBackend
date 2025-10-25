package com.komal.template_backend.Util;

public class MaskingUtil {
    public static String maskUniqueId(String value) {
        if (value == null || value.length() < 4) return "****";
        return "XXXX-XXXX-" + value.substring(value.length() - 4);
    }

    public static String maskAccountNumber(String value) {
        if (value == null || value.length() < 4) return "****";
        return "XXXXXX" + value.substring(value.length() - 4);
    }

    public static String maskIfsc(String value) {
        if (value == null || value.length() < 4) return "****";
        return value.substring(0, 4) + "XXXX";
    }

    public static String maskBankName(String value) {
        if (value == null) return "****";
        return value.substring(0, 2) + "****";
    }
}
