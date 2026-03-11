package org.backend.domain.auth.service;

import io.jsonwebtoken.Claims;
import org.backend.config.security.JwtProvider;
import org.backend.domain.admin.entity.Admin;
import org.backend.domain.admin.entity.AdminStatus;
import org.backend.domain.admin.repository.AdminRepository;
import org.backend.domain.auth.dto.request.LoginRequest;
import org.backend.domain.auth.dto.request.RefreshRequest;
import org.backend.domain.auth.dto.response.LoginResponse;
import org.backend.domain.auth.dto.response.MeResponse;
import org.backend.domain.auth.dto.response.TokenResponse;
import org.backend.domain.auth.entity.RefreshToken;
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

        // 비활성 계정 차단
        if (admin.getStatus() != AdminStatus.ACTIVE) {
            throw new IllegalArgumentException("비활성화된 계정입니다.");
        }

        if (Boolean.TRUE.equals(admin.getGoogle())) {
            throw new IllegalArgumentException("구글 로그인 계정입니다. 구글 로그인을 이용하세요.");
        }

        if (admin.getPassword() == null || admin.getPassword().isBlank()) {
            throw new IllegalArgumentException("비밀번호가 설정되지 않은 계정입니다.");
        }

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String role = admin.getRole().name();
        String accessToken = jwtProvider.generateAccessToken(admin.getId(), admin.getEmail(), role);
        String refreshToken = jwtProvider.generateRefreshToken(admin.getId());

        upsertRefreshToken(admin.getId(), refreshToken);

        return new LoginResponse(accessToken, refreshToken, admin.getId(), admin.getEmail(), role,  admin.getName());
    }

    @Transactional(readOnly = true)
    public MeResponse me(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보를 찾을 수 없습니다."));

        return new MeResponse(admin.getId(), admin.getEmail(), admin.getRole().name());
    }

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

        if (!refreshToken.equals(saved.getToken())) {
            refreshTokenRepository.delete(saved);
            throw new IllegalArgumentException("리프레시 토큰이 일치하지 않습니다. 다시 로그인하세요.");
        }

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보를 찾을 수 없습니다."));

        // 비활성 계정이면 refresh로 재발급 못하게 차단
        if (admin.getStatus() != AdminStatus.ACTIVE) {
            refreshTokenRepository.deleteByAdminId(adminId); // 깔끔하게 RT도 정리(선택이지만 추천)
            throw new IllegalArgumentException("비활성화된 계정입니다.");
        }

        String role = admin.getRole().name();
        String newAccess = jwtProvider.generateAccessToken(admin.getId(), admin.getEmail(), role);
        String newRefresh = jwtProvider.generateRefreshToken(admin.getId());

        upsertRefreshToken(admin.getId(), newRefresh);

        return new TokenResponse(newAccess, newRefresh);
    }

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