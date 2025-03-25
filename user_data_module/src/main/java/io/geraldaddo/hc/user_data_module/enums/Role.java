package io.geraldaddo.hc.user_data_module.enums;

public enum Role {

    PATIENT("PATIENT"),
    DOCTOR("DOCTOR"),
    ADMIN("ADMIN");
    private final String name;

    Role(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
