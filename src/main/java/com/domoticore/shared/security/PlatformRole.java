package com.domoticore.shared.security;

import java.util.Arrays;
import java.util.Locale;

public enum PlatformRole {
    ADMIN,
    MODERATOR,
    USER;

    public static PlatformRole from(String role) {
        if (role == null || role.isBlank()) {
            return USER;
        }
        String normalized = role.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "admin" -> ADMIN;
            case "moderator", "moderador" -> MODERATOR;
            default -> USER;
        };
    }
}
