package com.tawseela.util;

public final class PhoneNormalizer {

    private PhoneNormalizer() {}

    /**
     * Normalizes mobile to E.164 when possible. Admin bootstrap account uses literal {@code admin}.
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String t = raw.trim();
        if (t.isEmpty()) {
            return "";
        }
        if ("admin".equalsIgnoreCase(t)) {
            return "admin";
        }
        String digits = t.replaceAll("[^0-9+]", "");
        if (digits.startsWith("+")) {
            return digits;
        }
        if (digits.startsWith("00")) {
            return "+" + digits.substring(2);
        }
        return digits;
    }
}
