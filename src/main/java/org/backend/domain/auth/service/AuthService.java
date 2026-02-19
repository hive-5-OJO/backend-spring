// AuthService.java
package org.backend.domain.auth.service;

import org.backend.domain.auth.dto.LoginRequest;
import org.backend.domain.auth.dto.LoginResponse;
import org.backend.domain.auth.entity.Admin;
import org.backend.domain.auth.repository.AdminRepository;
import org.backend.config.security.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthService(AdminRepository adminRepository,
                       PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // google 계정 전용이면 비번 로그인 막기 (정책은 필요하면 바꿔도 됨)
        if (Boolean.TRUE.equals(admin.getGoogle())) {
            throw new IllegalArgumentException("구글 로그인 계정입니다. 구글 로그인을 이용하세요.");
        }

        if (admin.getPassword() == null || admin.getPassword().isBlank()) {
            throw new IllegalArgumentException("비밀번호가 설정되지 않은 계정입니다.");
        }

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtProvider.generateAccessToken(admin.getId(), admin.getEmail(), admin.getRole());

        return new LoginResponse(token, admin.getId(), admin.getEmail(), admin.getRole());
    }
}
