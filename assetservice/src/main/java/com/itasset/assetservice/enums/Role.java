package com.itasset.assetservice.enums;

// typed enum: each role carries a human-friendly label (behavior/data), not just a bare name.
// Day 15 study point — an enum can hold fields + methods, unlike a raw String.
public enum Role {
    ADMIN("Administrator"),
    EMPLOYEE("Employee");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
