// backend-spring/src/main/java/org/backend/domain/auth/controller/AuthController.java
package org.backend.domain.auth.controller;

import jakarta.validation.Valid;
import org.backend.domain.auth.dto.*;
import org.backend.domain.auth.security.AdminPrincipal;
import org.backend.domain.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * 토큰 검증용 (Authorization: Bearer <accessToken>)
     */
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication authentication) {
        AdminPrincipal principal = extractPrincipal(authentication);
        return ResponseEntity.ok(authService.me(principal.getAdminId()));
    }

    /**
     * refreshToken으로 accessToken 재발급 (+ refresh 로테이션)
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    /**
     * 로그아웃: DB refresh 폐기
     * (Authorization: Bearer <accessToken>)
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        AdminPrincipal principal = extractPrincipal(authentication);
        authService.logout(principal.getAdminId());
        return ResponseEntity.ok().build();
    }

    private AdminPrincipal extractPrincipal(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }

        Object principal = authentication.getPrincipal();

        // JwtAuthenticationFilter가 AdminPrincipal을 넣는다
        if (principal instanceof AdminPrincipal ap) {
            return ap;
        }

        //
        if (principal instanceof String s) {
            try {
                Long adminId = Long.parseLong(s);
                return new AdminPrincipal(adminId, null, null);
            } catch (Exception ignored) {
            }
        }

        throw new IllegalArgumentException("인증 주체 정보가 올바르지 않습니다.");
    }
}