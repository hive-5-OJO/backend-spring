package org.backend.domain.auth.service;

import io.jsonwebtoken.Claims;
import org.backend.config.security.JwtProvider;
import org.backend.domain.auth.dto.*;
import org.backend.domain.auth.entity.Admin;
import org.backend.domain.auth.entity.RefreshToken;
import org.backend.domain.auth.repository.AdminRepository;
import org.backend.domain.auth.repository.RefreshTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AdminRepository adminRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthService(AdminRepository adminRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider) {
        this.adminRepository = adminRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // 구글 계정은 비번 로그인 막기(정책 변경 가능)
        if (Boolean.TRUE.equals(admin.getGoogle())) {
            throw new IllegalArgumentException("구글 로그인 계정입니다. 구글 로그인을 이용하세요.");
        }

        if (admin.getPassword() == null || admin.getPassword().isBlank()) {
            throw new IllegalArgumentException("비밀번호가 설정되지 않은 계정입니다.");
        }

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.generateAccessToken(admin.getId(), admin.getEmail(), admin.getRole());
        String refreshToken = jwtProvider.generateRefreshToken(admin.getId());

        // refresh upsert 저장 (로그인 시점에 항상 최신 refresh로 교체)
        upsertRefreshToken(admin.getId(), refreshToken);

        return new LoginResponse(accessToken, refreshToken, admin.getId(), admin.getEmail(), admin.getRole());
    }

    @Transactional(readOnly = true)
    public MeResponse me(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보를 찾을 수 없습니다."));
        return new MeResponse(admin.getId(), admin.getEmail(), admin.getRole());
    }

    /**
     * refresh → access 재발급 (+ refresh 로테이션)
     */
    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtProvider.validate(refreshToken) || !jwtProvider.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        Claims claims = jwtProvider.getClaims(refreshToken);
        Long adminId = Long.parseLong(claims.getSubject());

        RefreshToken saved = refreshTokenRepository.findByAdminId(adminId)
                .orElseThrow(() -> new IllegalArgumentException("리프레시 토큰이 만료되었거나 로그아웃 상태입니다."));

        // DB에 저장된 refresh와 일치해야만 인정(탈취 토큰 차단)
        if (!refreshToken.equals(saved.getToken())) {
            refreshTokenRepository.delete(saved);
            throw new IllegalArgumentException("리프레시 토큰이 일치하지 않습니다. 다시 로그인하세요.");
        }

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보를 찾을 수 없습니다."));

        String newAccess = jwtProvider.generateAccessToken(admin.getId(), admin.getEmail(), admin.getRole());
        String newRefresh = jwtProvider.generateRefreshToken(admin.getId());

        // 로테이션: 새 refresh로 덮어쓰기
        upsertRefreshToken(admin.getId(), newRefresh);

        return new TokenResponse(newAccess, newRefresh);
    }

    /**
     * 로그아웃: refresh 폐기
     */
    @Transactional
    public void logout(Long adminId) {
        refreshTokenRepository.deleteByAdminId(adminId);
    }

    private void upsertRefreshToken(Long adminId, String token) {
        RefreshToken rt = refreshTokenRepository.findByAdminId(adminId)
                .orElseGet(() -> RefreshToken.builder().adminId(adminId).build());

        rt.updateToken(token);
        refreshTokenRepository.save(rt);
    }
}