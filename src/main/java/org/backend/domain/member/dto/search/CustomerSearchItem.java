package org.backend.domain.member.dto.search;

import java.time.LocalDateTime;

public record CustomerSearchItem(
        Long memberId,
        String name,
        String phone,
        String email,
        LocalDateTime createdAt,
        String status,
        String statusLabel
) {}