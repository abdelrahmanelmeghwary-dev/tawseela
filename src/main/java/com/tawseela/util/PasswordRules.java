package com.tawseela.util;

public final class PasswordRules {

    /**
     * Minimum 8 chars, at least one uppercase, one lowercase, one digit, one special character.
     */
    public static final String REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{8,}$";

    public static final String MESSAGE =
            "Password must be at least 8 characters and include uppercase, lowercase, a number, and a special character";

    private PasswordRules() {}
}
