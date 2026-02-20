package org.backend.domain.auth.dto;

public record MeResponse(
        Long id,
        String email,
        String role
) {
}