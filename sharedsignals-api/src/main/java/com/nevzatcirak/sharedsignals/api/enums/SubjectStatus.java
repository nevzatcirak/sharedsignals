package com.nevzatcirak.sharedsignals.api.enums;

public enum SubjectStatus {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected");

    private final String value;

    SubjectStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static SubjectStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (SubjectStatus b : SubjectStatus.values()) {
            if (String.valueOf(b.value).equalsIgnoreCase(value) || b.name().equalsIgnoreCase(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "' for SubjectStatus");
    }
}