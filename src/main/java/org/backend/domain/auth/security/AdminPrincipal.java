package org.backend.domain.auth.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AdminPrincipal implements UserDetails {

    private final Long adminId;
    private final String email;
    private final String role;

    public AdminPrincipal(Long adminId, String email, String role) {
        this.adminId = adminId;
        this.email = email;
        this.role = role;
    }

    public Long getAdminId() {
        return adminId;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // role 예: "ROLE_ADMIN" 형태로 들어온다고 가정
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return null; // JWT 방식이라 password는 principal에서 안 씀
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}