package com.arnor4eck.springkod.entity.user;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    USER("USER"), ADMIN("ADMIN");

    private final String authority;

    Role(String authority){
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return "ROLE_" + this.authority;
    }
}
