package com.domoticore.iam.domain;

public enum AccountType {
    SMART_HOME,
    SMALL_BUSINESS;

    public static AccountType fromJson(String value) {
        if (value == null) {
            return null;
        }
        return switch (value.toLowerCase().replace('_', '-')) {
            case "smart-home" -> SMART_HOME;
            case "small-business" -> SMALL_BUSINESS;
            default -> throw new IllegalArgumentException("Unknown account type: " + value);
        };
    }

    public String toJson() {
        return switch (this) {
            case SMART_HOME -> "smart-home";
            case SMALL_BUSINESS -> "small-business";
        };
    }
}
