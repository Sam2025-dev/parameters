package com.pojo.parameters.handlers;

/**
 * Naming and field-selection rules shared by {@code @Data} handlers, matching
 * {@code lombok.core.handlers.HandlerUtil}.
 */
public final class HandlerUtil {

    private HandlerUtil() {
    }

    public static boolean fieldQualifies(String fieldName, boolean isStatic) {
        if (isStatic || fieldName == null || fieldName.isEmpty()) {
            return false;
        }
        return !fieldName.startsWith("$");
    }

    public static String toGetterName(String fieldName, boolean primitiveBoolean) {
        return toAccessorName(fieldName, primitiveBoolean, "is", "get");
    }

    public static String toSetterName(String fieldName, boolean primitiveBoolean) {
        return toAccessorName(fieldName, primitiveBoolean, "set", "set");
    }

    private static String toAccessorName(
            String fieldName, boolean primitiveBoolean, String booleanPrefix, String normalPrefix) {
        if (fieldName == null || fieldName.isEmpty()) {
            return null;
        }
        if (primitiveBoolean && fieldName.startsWith("is") && fieldName.length() > 2
                && !Character.isLowerCase(fieldName.charAt(2))) {
            return booleanPrefix + fieldName.substring(2);
        }
        return buildAccessorName(primitiveBoolean ? booleanPrefix : normalPrefix, fieldName);
    }

    public static String buildAccessorName(String prefix, String suffix) {
        if (suffix.isEmpty()) {
            return prefix;
        }
        if (prefix.isEmpty()) {
            return suffix;
        }
        return prefix + capitalize(suffix);
    }

    static String capitalize(String suffix) {
        if (suffix.length() > 1 && Character.isUpperCase(suffix.charAt(1))) {
            return Character.toUpperCase(suffix.charAt(0)) + suffix.substring(1);
        }
        return Character.toUpperCase(suffix.charAt(0)) + suffix.substring(1);
    }

    public static int primeForHashcode() {
        return 59;
    }
}
