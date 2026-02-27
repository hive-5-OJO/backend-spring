package org.backend.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import org.backend.domain.auth.entity.AccessLog;
import org.backend.domain.auth.repository.AccessLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccessLogService {

    private final AccessLogRepository accessLogRepository;

    public AccessLogService(AccessLogRepository accessLogRepository) {
        this.accessLogRepository = accessLogRepository;
    }

    /**
     * 메인 트랜잭션이 실패해도 로그는 남기기 위해 REQUIRES_NEW
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(HttpServletRequest request, Long userId, String email, String role, int statusCode) {
        try {
            AccessLog log = AccessLog.builder()
                    .userId(userId)
                    .email(email)
                    .role(role)
                    .method(request.getMethod())
                    .path(request.getRequestURI())
                    .statusCode(statusCode)
                    .ip(extractClientIp(request))
                    .userAgent(trim(request.getHeader("User-Agent"), 255))
                    .build();

            accessLogRepository.save(log);
        } catch (Exception ignored) {
        }
    }

    private String trim(String v, int max) {
        if (v == null) return null;
        return v.length() <= max ? v : v.substring(0, max);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String xrip = request.getHeader("X-Real-IP");
        if (xrip != null && !xrip.isBlank()) return xrip.trim();

        return request.getRemoteAddr();
    }
}