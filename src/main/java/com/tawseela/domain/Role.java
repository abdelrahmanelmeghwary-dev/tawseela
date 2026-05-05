package com.tawseela.domain;

public enum Role {
    customer,
    driver,
    admin;

    public String springAuthority() {
        return "ROLE_" + name().toUpperCase();
    }
}
