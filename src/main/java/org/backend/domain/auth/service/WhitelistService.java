package org.backend.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.backend.domain.auth.repository.AuthWhitelistEmailRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WhitelistService {

    private final AuthWhitelistEmailRepository whitelistEmailRepository;

    // 옵션: 도메인 제한을 같이 걸고 싶으면 사용 (비어있으면 도메인 체크 스킵)
    @Value("${auth.whitelist.domain:}")
    private String allowedDomain;

    public void validate(String email) {
        // 1) 도메인 제한(옵션)
        if (allowedDomain != null && !allowedDomain.isBlank()) {
            if (!email.toLowerCase().endsWith("@" + allowedDomain.toLowerCase())) {
                throw new IllegalArgumentException("허용되지 않은 이메일 도메인입니다.");
            }
        }

        // 2) 화이트리스트 테이블 체크
        if (!whitelistEmailRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("화이트리스트에 등록되지 않은 이메일입니다.");
        }
    }
}