package org.backend.domain.admin.entity;

public enum AdminRole {
    CS,
    MARKETING,
    ADMIN;

    public String asAuthority() {
        return "ROLE_" + this.name();
    }
}