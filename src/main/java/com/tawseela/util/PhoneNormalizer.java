package com.tawseela.util;

public final class PhoneNormalizer {

    private PhoneNormalizer() {}

    public static String normalize(String phone) {
        if (phone == null) {
            return "";
        }
        String digits = phone.replaceAll("\\D", "");
        return digits.isEmpty() ? "" : "+" + digits;
    }
}
